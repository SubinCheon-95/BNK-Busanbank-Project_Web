package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.CallStatus;
import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;

import kr.co.busanbank.call.service.CallCustomerWsNotifier;
import kr.co.busanbank.call.service.CallQueueKeys;
import kr.co.busanbank.call.service.CallWsAssignNotifier;
import kr.co.busanbank.call.service.VoiceCallQueueService;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cs/call/voice")
@RequiredArgsConstructor
public class AgentVoiceCallController {

    private final VoiceCallQueueService service;
    private final MemberMapper memberMapper;

    // ✅ 고객에게 push할 노티파이어
    private final CallCustomerWsNotifier customerWsNotifier;

    // ✅ 추가 1) status-with-token이 읽는 세션 해시를 쓰기 위해 필요
    private final StringRedisTemplate redis;

    // ✅ 추가 2) keys.sessionKey(sessionId) 사용
    private final CallQueueKeys keys;

    @Value("${call.acceptAllowQueryId:false}")
    private boolean acceptAllowQueryId;

    @GetMapping("/waiting")
    public List<VoiceWaitingSessionDTO> waiting(Authentication authentication) {
        String consultantId = resolveConsultantId(authentication, null);
        log.info("[VOICE] waiting called by={}", consultantId);
        List<VoiceWaitingSessionDTO> list = service.getWaitingList(50);
        log.info("[VOICE] waiting size={}", list.size());
        return list;
    }

    /** 수락 */
    @PostMapping("/{sessionId}/accept")
    public ResponseEntity<?> accept(@PathVariable String sessionId,
                                    @RequestParam(required = false) String consultantId,
                                    Authentication authentication) {

        String cid = resolveConsultantId(authentication, consultantId);
        if (cid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "reason", "UNAUTHORIZED"));
        }

        Map<String, Object> result = service.accept(sessionId, cid);
        if (!(Boolean.TRUE.equals(result.get("ok")))) {
            return ResponseEntity.ok(result); // AGENT_BUSY / NOT_WAITING 등 그대로 내려줌
        }

        // ✅ (여기부터가 핵심)
        // ✅ agoraChannel은 서버 규칙으로 생성
        String agoraChannel = "voice_" + sessionId;

        // =========================================================
        // ✅ [추가 위치] 바로 여기! (service.accept 성공 직후)
        // status-with-token 컨트롤러가 보는 Redis 해시 상태를 올려줘야
        // Flutter가 토큰을 발급받을 수 있습니다.
        // =========================================================
        String sKey = keys.sessionKey(sessionId);

        redis.opsForHash().put(sKey, "callStatus", CallStatus.CALL_ASSIGNED.name());
        redis.opsForHash().put(sKey, "callAgentId", cid);
        redis.opsForHash().put(sKey, "callAssignedAt", Instant.now().toString());
        redis.opsForHash().put(sKey, "agoraChannel", agoraChannel); // 선택(있으면 편함)
        // =========================================================
        redis.expire(sKey, Duration.ofMinutes(30));

        // ✅ 고객에게 실시간 push
        customerWsNotifier.notifyAccepted(sessionId, cid, agoraChannel);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "sessionId", sessionId,
                "consultantId", cid,
                "agoraChannel", agoraChannel
        ));
    }

    /** 종료 */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> end(@PathVariable String sessionId,
                                 @RequestParam(required = false) String consultantId,
                                 Authentication authentication) {

        String cid = resolveConsultantId(authentication, consultantId);
        if (cid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "reason", "UNAUTHORIZED"));
        }

        service.end(sessionId, cid);

        // =========================================================
        // ✅ 종료 시에도 해시 상태를 ENDED로 내려서
        // Flutter 폴링/화면이 즉시 종료 판단할 수 있게 해주세요.
        // =========================================================
        String sKey = keys.sessionKey(sessionId);
        redis.opsForHash().put(sKey, "callStatus", CallStatus.CALL_ENDED.name());
        redis.opsForHash().put(sKey, "callEndedAt", Instant.now().toString());
        // =========================================================
        // 종료 후에는 짧게 보관하고 삭제되게 (예: 5분)
        redis.expire(sKey, Duration.ofMinutes(5));

        // ✅ 고객에게 실시간 push
        customerWsNotifier.notifyEnded(sessionId, cid);

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId));
    }

    private String resolveConsultantId(Authentication authentication, String consultantId) {
        if (acceptAllowQueryId && StringUtils.hasText(consultantId)) {
            return consultantId.trim();
        }
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return authentication.getName();
    }
}
