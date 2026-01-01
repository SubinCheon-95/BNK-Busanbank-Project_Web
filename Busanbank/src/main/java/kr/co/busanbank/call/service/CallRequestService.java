package kr.co.busanbank.call.service;

import kr.co.busanbank.call.CallStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class CallRequestService {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    public CallRequestService(StringRedisTemplate redis, CallQueueKeys keys) {
        this.redis = redis;
        this.keys = keys;
    }

    public void enqueueCall(String sessionId, String inquiryType) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        String sKey = keys.sessionKey(sessionId);
        String qKey = keys.callQueue("default");

        long now = Instant.now().toEpochMilli();

        String raw = (String) redis.opsForHash().get(sKey, "callStatus");
        CallStatus cur = CallStatus.from(raw);

        if (cur == CallStatus.CALL_WAITING || cur == CallStatus.CALL_ASSIGNED || cur == CallStatus.CALL_CONNECTED) {
            log.info("ℹ️ enqueueCall ignored. sessionId={}, callStatus={}", sessionId, cur);
            return;
        }

        cur.assertTransitTo(CallStatus.CALL_WAITING);

        redis.opsForHash().putAll(sKey, Map.of(
                "callStatus", CallStatus.CALL_WAITING.name(),
                "callEnqueuedAt", String.valueOf(now),
                "callRetryCount", "0"
        ));

        redis.opsForZSet().add(qKey, sessionId, now);

        log.info("✅ Call enqueued. sessionId={}, inquiryType={}, queueKey={}", sessionId, inquiryType, qKey);
    }
}
