package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.CallStatus;
import kr.co.busanbank.call.dto.CallTokenResponse;
import kr.co.busanbank.call.service.CallQueueKeys;
import kr.co.busanbank.call.service.CallTokenService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/call")
public class CallStatusWithTokenController {

    private static final String TOKEN_CONSUMED_FLAG_PREFIX = "chat:call:tokenConsumed:"; // {sessionId}:{role}

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;
    private final CallTokenService callTokenService;

    public CallStatusWithTokenController(StringRedisTemplate redis,
                                         CallQueueKeys keys,
                                         CallTokenService callTokenService) {
        this.redis = redis;
        this.keys = keys;
        this.callTokenService = callTokenService;
    }

    @PostMapping("/{sessionId}/status-with-token")
    public ResponseEntity<?> statusWithToken(@PathVariable String sessionId,
                                             @RequestBody(required = false) StatusWithTokenRequest req) {

        String role = (req == null || req.getRole() == null || req.getRole().isBlank())
                ? "CUSTOMER"
                : req.getRole().trim();

        String sKey = keys.sessionKey(sessionId);

        // ✅ 응답 Map.of 쓰면 null에서 500 터지므로 LinkedHashMap 사용
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("ok", true);
        res.put("sessionId", sessionId);

        // ✅ 세션 없으면 종료로 응답
        if (Boolean.FALSE.equals(redis.hasKey(sKey))) {
            res.put("callStatus", CallStatus.CALL_ENDED.name());
            res.put("ended", true);
            res.put("reason", "session_not_found");
            // token은 없음(null 가능)
            res.put("token", null);
            res.put("tokenConsumed", true);
            return ResponseEntity.ok(res);
        }

        String rawStatus = (String) redis.opsForHash().get(sKey, "callStatus");
        CallStatus status = safeStatus(rawStatus);

        // 메타
        String agentId = (String) redis.opsForHash().get(sKey, "callAgentId");
        String assignedAt = (String) redis.opsForHash().get(sKey, "callAssignedAt");
        String connectedAt = (String) redis.opsForHash().get(sKey, "callConnectedAt");
        String endedAt = (String) redis.opsForHash().get(sKey, "callEndedAt");

        String agoraChannel = "cs_" + sessionId;

        // token-consumed 플래그
        String consumedKey = TOKEN_CONSUMED_FLAG_PREFIX + sessionId + ":" + role;
        boolean tokenConsumed = Boolean.TRUE.equals(redis.hasKey(consumedKey));

        // 토큰 지급 조건
        boolean tokenShouldBeProvided = (status == CallStatus.CALL_ASSIGNED || status == CallStatus.CALL_CONNECTED);

        CallTokenResponse token = null;
        if (tokenShouldBeProvided && !tokenConsumed) {
            token = callTokenService.issue(sessionId, role);
        }

        res.put("callStatus", status.name());
        res.put("callAgentId", agentId == null ? "" : agentId);
        res.put("agoraChannel", agoraChannel);

        // ✅ null 안전
        res.put("token", token);
        res.put("tokenConsumed", tokenConsumed);

        res.put("callAssignedAt", assignedAt == null ? "" : assignedAt);
        res.put("callConnectedAt", connectedAt == null ? "" : connectedAt);
        res.put("callEndedAt", endedAt == null ? "" : endedAt);

        return ResponseEntity.ok(res);
    }

    private CallStatus safeStatus(String raw) {
        try {
            if (raw == null || raw.isBlank()) return CallStatus.CALL_ENDED; // 빈 값이면 종료로 취급
            CallStatus s = CallStatus.from(raw);
            return (s == null) ? CallStatus.CALL_ENDED : s;
        } catch (Exception e) {
            log.warn("⚠️ CallStatus parse failed. raw={}", raw, e);
            return CallStatus.CALL_ENDED;
        }
    }

    @Data
    public static class StatusWithTokenRequest {
        private String role; // CUSTOMER / AGENT
    }
}
