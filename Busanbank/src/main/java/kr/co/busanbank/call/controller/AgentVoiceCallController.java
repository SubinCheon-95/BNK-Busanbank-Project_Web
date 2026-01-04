package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;

import kr.co.busanbank.call.service.CallCustomerWsNotifier;
import kr.co.busanbank.call.service.CallWsAssignNotifier;
import kr.co.busanbank.call.service.VoiceCallQueueService;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        // ✅ agoraChannel은 서버에서 규칙으로 만들거나(권장) accept 결과에 포함시켜도 됨
        String agoraChannel = "voice_" + sessionId;

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
