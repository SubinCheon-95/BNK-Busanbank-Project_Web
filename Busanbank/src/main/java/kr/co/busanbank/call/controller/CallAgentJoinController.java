package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.CallStatus;
import kr.co.busanbank.call.service.CallQueueKeys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/call")
public class CallAgentJoinController {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    public CallAgentJoinController(StringRedisTemplate redis, CallQueueKeys keys) {
        this.redis = redis;
        this.keys = keys;
    }

    /**
     * 상담사 join 성공 콜백
     * 예) POST /api/call/{sessionId}/agent-joined
     */
    @PostMapping("/{sessionId}/agent-joined")
    public ResponseEntity<?> agentJoined(@PathVariable String sessionId,
                                         @RequestBody AgentJoinedRequest req) {

        String sKey = keys.sessionKey(sessionId);

        CallStatus cur = CallStatus.from((String) redis.opsForHash().get(sKey, "callStatus"));
        String assignedAgentId = (String) redis.opsForHash().get(sKey, "callAgentId");

        // 1) 기본 검증: ASSIGNED 상태에서만 CONNECTED로
        if (cur != CallStatus.CALL_ASSIGNED) {
            log.info("ℹ️ agentJoined ignored (status mismatch). sessionId={}, cur={}", sessionId, cur);
            return ResponseEntity.ok(Map.of("ok", true, "ignored", true, "reason", "status_mismatch"));
        }

        // 2) 배정된 agent와 요청 agent가 다르면 무시 (재배정 경합 방지)
        if (assignedAgentId != null && !assignedAgentId.isBlank()
                && req.getAgentId() != null && !req.getAgentId().isBlank()
                && !assignedAgentId.equals(req.getAgentId())) {
            log.warn("⚠️ agentJoined ignored (agent mismatch). sessionId={}, assigned={}, req={}",
                    sessionId, assignedAgentId, req.getAgentId());
            return ResponseEntity.ok(Map.of("ok", true, "ignored", true, "reason", "agent_mismatch"));
        }

        // 3) 상태 전이
        cur.assertTransitTo(CallStatus.CALL_CONNECTED);

        long now = Instant.now().toEpochMilli();

        redis.opsForHash().putAll(sKey, Map.of(
                "callStatus", CallStatus.CALL_CONNECTED.name(),
                "callConnectedAt", String.valueOf(now),
                // 요청에 들어온 agora uid 저장(선택)
                "callAgentAgoraUid", req.getAgoraUid() == null ? "" : req.getAgoraUid()
        ));

        // 4) watch 제거
        redis.opsForZSet().remove(keys.assignedWatchZset(), sessionId);

        log.info("✅ call connected. sessionId={}, agentId={}, agoraUid={}", sessionId, assignedAgentId, req.getAgoraUid());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "sessionId", sessionId,
                "callStatus", CallStatus.CALL_CONNECTED.name()
        ));
    }

    @Data
    public static class AgentJoinedRequest {
        /**
         * 상담사 ID (배정된 상담사와 일치 검증용)
         */
        private String agentId;

        /**
         * 상담사 Agora UID (선택)
         */
        private String agoraUid;
    }
}
