package kr.co.busanbank.call.service;

import kr.co.busanbank.call.CallStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

/**
 * 4번: ASSIGNED 타임아웃 감시 워커
 * - call:assigned:watch(ZSET)에 들어간 세션을 훑어서
 * - CALL_ASSIGNED 상태인데 assignedTimeoutMs 초과면:
 *   1) 상담사 BUSY 해제 + readyZset 복귀 + load 감소
 *   2) 세션 callStatus를 CALL_WAITING으로 되돌림(재배정)
 *   3) 대기열(call:waiting:default)에 재삽입
 *   4) watch ZSET에서 제거
 *
 * ✅ 운영 포인트:
 * - 상담사 앱이 배정 이벤트를 못 받거나 Agora join 실패하면 여기서 자동 복구됩니다.
 */
@Slf4j
@Component
public class CallAssignWatchdogWorker {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    @Value("${chat.call.assignedTimeoutMs:15000}")
    private long assignedTimeoutMs;

    // ✅ 기존 yml(chat.redis.consultant.*) 그대로 재사용
    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String consultantReadyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String consultantLoadZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String consultantStatusPrefix;

    public CallAssignWatchdogWorker(StringRedisTemplate redis, CallQueueKeys keys) {
        this.redis = redis;
        this.keys = keys;
    }

    /**
     * 0.5초마다 타임아웃 세션 처리
     * - 너무 자주/너무 크게 훑지 않도록 "rangeByScore"로 타임아웃 대상만 뽑습니다.
     */
    @Scheduled(fixedDelayString = "${chat.call.watchdog.delayMs:500}")
    public void tick() {
        long now = Instant.now().toEpochMilli();
        long cutoff = now - assignedTimeoutMs;

        String watchKey = keys.assignedWatchZset();

        // score <= cutoff 인 세션만 조회
        Set<String> timedOutSessionIds = redis.opsForZSet().rangeByScore(watchKey, 0, cutoff);
        if (timedOutSessionIds == null || timedOutSessionIds.isEmpty()) return;

        for (String sessionId : timedOutSessionIds) {
            try {
                handleTimeout(sessionId, now);
            } catch (Exception e) {
                log.error("❌ watchdog handleTimeout fail. sessionId={}", sessionId, e);
            }
        }
    }

    private void handleTimeout(String sessionId, long now) {
        String sKey = keys.sessionKey(sessionId);

        CallStatus cur = CallStatus.from((String) redis.opsForHash().get(sKey, "callStatus"));
        if (cur != CallStatus.CALL_ASSIGNED) {
            // 이미 CONNECTED/ENDED 등으로 진행됐으면 watch만 정리
            redis.opsForZSet().remove(keys.assignedWatchZset(), sessionId);
            return;
        }

        // 상담사 id 확인
        String consultantId = (String) redis.opsForHash().get(sKey, "callAgentId");

        // retryCount 증가
        int retry = 0;
        try {
            String raw = (String) redis.opsForHash().get(sKey, "callRetryCount");
            retry = (raw == null || raw.isBlank()) ? 0 : Integer.parseInt(raw);
        } catch (Exception ignore) {
        }
        retry++;

        // 상담사 BUSY 해제 및 ready 복귀
        if (consultantId != null && !consultantId.isBlank()) {
            redis.opsForValue().set(consultantStatusPrefix + consultantId, "READY");
            redis.opsForZSet().incrementScore(consultantLoadZset, consultantId, -1);

            // ready 큐에 재등록 (score는 0으로 단순 처리)
            redis.opsForZSet().add(consultantReadyZset, consultantId, 0.0);
        }

        // watch 제거
        redis.opsForZSet().remove(keys.assignedWatchZset(), sessionId);

        // 재시도 한도 (원하면 yml로 빼도 됨)
        int maxRetry = 3;
        if (retry > maxRetry) {
            // 재시도 초과 -> 종료 처리
            redis.opsForHash().put(sKey, "callStatus", CallStatus.CALL_ENDED.name());
            redis.opsForHash().putAll(sKey, java.util.Map.of(
                    "callRetryCount", String.valueOf(retry)
            ));
            log.warn("⚠️ call assign timeout exceeded maxRetry. sessionId={}, retry={}", sessionId, retry);
            return;
        }

        // 세션을 WAITING으로 되돌리고 재큐잉
        CallStatus.CALL_ASSIGNED.assertTransitTo(CallStatus.CALL_WAITING);

        redis.opsForHash().putAll(sKey, java.util.Map.of(
                "callStatus", CallStatus.CALL_WAITING.name(),
                "callAgentId", "",
                "callAssignedAt", "",
                "callRetryCount", String.valueOf(retry)
        ));

        // ✅ 기본은 default 큐로 재삽입 (원하면 inquiryType 기반으로 바꿀 수 있음)
        String qKey = keys.callQueue("default");
        redis.opsForZSet().add(qKey, sessionId, (double) now);

        log.warn("🔁 call re-queued by watchdog. sessionId={}, retry={}, queue={}", sessionId, retry, qKey);
    }
}
