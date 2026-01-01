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
public class CallAgentJoinService {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    @Value("${chat.call.assignedWatchZset:chat:call:assigned:watch}")
    private String assignedWatchZset;

    public CallAgentJoinService(StringRedisTemplate redis, CallQueueKeys keys) {
        this.redis = redis;
        this.keys = keys;
    }

    /**
     * 상담사가 Agora join 성공했음을 서버에 반영
     * - callStatus: CALL_CONNECTED
     * - callAgentId, callAgentAgoraUid 저장
     * - assignedWatchZset에서 제거해서 watchdog 재배정 루프 종료
     */
    public void markAgentJoined(String sessionId, String consultantId, String agoraUid) {
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId required");
        if (consultantId == null || consultantId.isBlank()) throw new IllegalArgumentException("consultantId required");

        String sKey = keys.sessionKey(sessionId);

        String now = String.valueOf(Instant.now().toEpochMilli());

        // ✅ 상태 전환
        redis.opsForHash().putAll(sKey, Map.of(
                "callStatus", CallStatus.CALL_CONNECTED.name(),
                "callAgentId", consultantId,
                "callAgentAgoraUid", agoraUid == null ? "" : agoraUid,
                "callConnectedAt", now
        ));

        // ✅ watchdog 감시에서 제거(재배정 멈춤)
        redis.opsForZSet().remove(assignedWatchZset, sessionId);

        log.info("✅ agent joined -> CONNECTED. sessionId={}, consultantId={}, agoraUid={}",
                sessionId, consultantId, agoraUid);
    }
}
