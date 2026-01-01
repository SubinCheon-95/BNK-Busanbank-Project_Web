package kr.co.busanbank.call.service;

import kr.co.busanbank.call.CallStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CallAssignWorker {
    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;
    private final CallAssignProperties props;
    private final CallAssignNotifier notifier;

    // ✅ 기존 yml(chat.redis.consultant.*) 그대로 재사용
    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String consultantReadyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String consultantLoadZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String consultantStatusPrefix;

    @Value("${chat.redis.consultant.lockPrefix:chat:consultant:lock:}")
    private String consultantLockPrefix;

    @Value("${chat.call.assignedTimeoutMs:15000}")
    private long assignedTimeoutMs;

    public CallAssignWorker(StringRedisTemplate redis,
                            CallQueueKeys keys,
                            CallAssignProperties props,
                            ObjectProvider<CallAssignNotifier> notifierProvider) {
        this.redis = redis;
        this.keys = keys;
        this.props = props;
        // notifier 구현이 없으면 로그만 찍는 기본 동작
        this.notifier = notifierProvider.getIfAvailable(() -> (sessionId, consultantId, agoraChannel) ->
                log.info("📣 (noop notifier) assigned sessionId={}, consultantId={}, channel={}", sessionId, consultantId, agoraChannel)
        );
    }

    /**
     * 전화 배정 루프
     * - call:waiting ZSET에서 sessionId POP
     * - consultant:ready ZSET에서 consultantId POP
     * - 세션 HASH callStatus=CALL_ASSIGNED, callAgentId 저장
     * - assigned watch ZSET 등록 (타임아웃 감시용)
     * - 상담사에게 배정 이벤트 notify
     */
    @Scheduled(fixedDelayString = "${chat.call.assign.delayMs:150}")
    public void tick() {
        if (!props.isEnabled()) return;

        List<String> types = props.getQueueTypes();
        if (types == null || types.isEmpty()) {
            types = List.of("default"); // ✅ 타입 설정 안 하면 default 큐만
        }

        int processed = 0;
        for (String type : types) {
            while (processed < props.getMaxPerTick()) {
                boolean ok = assignOne(type);
                if (!ok) break; // 이 타입 큐가 비었거나 상담사 없으면 다음 타입으로
                processed++;
            }
            if (processed >= props.getMaxPerTick()) break;
        }
    }

    private boolean assignOne(String inquiryType) {
        String qKey = keys.callQueue(inquiryType);

        // 1) 대기열에서 1건 pop
        ZSetOperations.TypedTuple<String> tuple = redis.opsForZSet().popMin(qKey);
        if (tuple == null || tuple.getValue() == null) {
            return false;
        }

        String sessionId = tuple.getValue();
        String sKey = keys.sessionKey(sessionId);
        long now = Instant.now().toEpochMilli();

        // 2) 세션 상태 확인 (WAITING만 배정)
        CallStatus cur = CallStatus.from((String) redis.opsForHash().get(sKey, "callStatus"));
        if (cur != CallStatus.CALL_WAITING) {
            // 이미 취소/종료/다른 상태면 그냥 버림(큐는 후보일 뿐)
            log.info("ℹ️ skip assign (not waiting). sessionId={}, callStatus={}", sessionId, cur);
            return true; // 다음 건 계속 처리
        }

        // 3) 상담사 확보 (readyZset에서 pop)
        ZSetOperations.TypedTuple<String> ctuple = redis.opsForZSet().popMin(consultantReadyZset);
        if (ctuple == null || ctuple.getValue() == null) {
            // 상담사 없으면 다시 대기열로 되돌림(순서 유지: 기존 score 그대로 or now)
            redis.opsForZSet().add(qKey, sessionId, tuple.getScore() != null ? tuple.getScore() : (double) now);
            return false;
        }

        String consultantId = ctuple.getValue();

        // 4) 상담사 락(이중 안전장치) - 실패하면 상담사 되돌리고 세션도 되돌림
        String lockKey = consultantLockPrefix + consultantId;
        Boolean locked = redis.opsForValue().setIfAbsent(lockKey, sessionId, java.time.Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(locked)) {
            // 상담사 다시 ready로 복귀
            redis.opsForZSet().add(consultantReadyZset, consultantId, ctuple.getScore() != null ? ctuple.getScore() : 0.0);
            // 세션도 다시 큐로
            redis.opsForZSet().add(qKey, sessionId, tuple.getScore() != null ? tuple.getScore() : (double) now);
            return true;
        }

        try {
            // 5) 세션 상태 업데이트: ASSIGNED
            cur.assertTransitTo(CallStatus.CALL_ASSIGNED);

            redis.opsForHash().putAll(sKey, Map.of(
                    "callStatus", CallStatus.CALL_ASSIGNED.name(),
                    "callAgentId", consultantId,
                    "callAssignedAt", String.valueOf(now)
            ));

            // 6) assigned watch 등록(타임아웃 감시용)
            redis.opsForZSet().add(keys.assignedWatchZset(), sessionId, now);

            // 7) 상담사 상태/부하 갱신 (있으면 좋음)
            redis.opsForValue().set(consultantStatusPrefix + consultantId, "BUSY");
            redis.opsForZSet().incrementScore(consultantLoadZset, consultantId, 1);

            // 8) 상담사에게 통지(Agora 채널은 sessionId를 그대로 쓰는 걸 추천)
            String agoraChannel = sessionId;
            notifier.notifyAssigned(sessionId, consultantId, agoraChannel);

            log.info("✅ call assigned. sessionId={}, consultantId={}, type={}, qKey={}", sessionId, consultantId, inquiryType, qKey);
            return true;

        } catch (Exception e) {
            // 실패 시 복구
            log.error("❌ assignOne failed. sessionId={}, consultantId={}", sessionId, consultantId, e);

            // 상담사 상태 복구
            redis.opsForValue().set(consultantStatusPrefix + consultantId, "READY");
            redis.opsForZSet().incrementScore(consultantLoadZset, consultantId, -1);
            redis.opsForZSet().add(consultantReadyZset, consultantId, 0.0);

            // 세션 재큐잉
            redis.opsForZSet().add(qKey, sessionId, tuple.getScore() != null ? tuple.getScore() : (double) now);

            return true;
        } finally {
            // 락 해제(점유는 readyZset에서 pop으로 이미 표현됨)
            redis.delete(lockKey);
        }
    }
}
