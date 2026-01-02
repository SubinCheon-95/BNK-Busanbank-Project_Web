package kr.co.busanbank.call.service;

import kr.co.busanbank.call.CallStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class CallEndService {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String consultantReadyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String consultantLoadZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String consultantStatusPrefix;

    @Value("${chat.call.voice.waitingZset:call:voice:waiting}")
    private String voiceWaitingZset;

    public CallEndService(StringRedisTemplate redis, CallQueueKeys keys) {
        this.redis = redis;
        this.keys = keys;
    }

    /**
     * 통화 종료 (고객/상담사 공용)
     */
    public void end(String sessionId, String byAgentId, String reason) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        String sKey = keys.sessionKey(sessionId);
        long now = Instant.now().toEpochMilli();

        // 🔒 세션이 없으면 그냥 정리만
        if (Boolean.FALSE.equals(redis.hasKey(sKey))) {
            cleanupQueues(sessionId);
            return;
        }

        CallStatus cur = CallStatus.from((String) redis.opsForHash().get(sKey, "callStatus"));
        if (cur == CallStatus.CALL_ENDED || cur == CallStatus.NONE) {
            cleanupQueues(sessionId);
            return;
        }

        String agentId = (String) redis.opsForHash().get(sKey, "callAgentId");

        // 상태 종료로 확정
        redis.opsForHash().putAll(sKey, Map.of(
                "callStatus", CallStatus.CALL_ENDED.name(),
                "callEndedAt", String.valueOf(now),
                "callEndReason", reason == null ? "" : reason,
                "callEndedBy", byAgentId == null ? "" : byAgentId
        ));

        // 대기/감시 큐 정리
        cleanupQueues(sessionId);

        // 상담사 복귀 처리 (한 번만)
        if (agentId != null && !agentId.isBlank()) {

            String statusKey = consultantStatusPrefix + agentId;
            String curStatus = redis.opsForValue().get(statusKey);

            // 🔒 이미 READY면 재처리 안 함
            if (!"READY".equals(curStatus)) {
                redis.opsForValue().set(statusKey, "READY");

                // load 음수 방지
                Double curLoad = redis.opsForZSet().score(consultantLoadZset, agentId);
                if (curLoad != null && curLoad > 0) {
                    redis.opsForZSet().incrementScore(consultantLoadZset, agentId, -1);
                }

                // READY 큐에 재등록
                redis.opsForZSet().add(consultantReadyZset, agentId, 0.0);
            }
        }

        log.info("✅ call ended. sessionId={}, fromStatus={}, agentId={}, endedBy={}, reason={}",
                sessionId, cur, agentId, byAgentId, reason);
    }

    private void cleanupQueues(String sessionId) {
        redis.opsForZSet().remove(keys.assignedWatchZset(), sessionId);
        // 기존(채팅콜) 큐
        redis.opsForZSet().remove(keys.callQueue("default"), sessionId);

        // ✅ 전화(voice) 큐
        redis.opsForZSet().remove(voiceWaitingZset, sessionId);
    }
}
