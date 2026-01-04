package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.service.CallWsAssignNotifier;
import kr.co.busanbank.call.service.VoiceCallQueueService;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/call/voice")
@RequiredArgsConstructor
public class ApiVoiceCallController {

    private final VoiceCallQueueService service;
    private final MemberMapper memberMapper;

    // ✅ 상담사에게 broadcast(VOICE_ENQUEUED) 하려면 주입
    private final CallWsAssignNotifier agentWsNotifier;

    @PostMapping("/enqueue/{sessionId}")
    public ResponseEntity<?> enqueue(@PathVariable String sessionId,
                                     Authentication authentication) {

        if (!StringUtils.hasText(sessionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required");
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "reason", "UNAUTHORIZED"));
        }

        String userId = authentication.getName(); // JWT userId
        UsersDTO user = memberMapper.findByUserId(userId);

        log.info("[VOICE][API] enqueue sessionId={} userId={} userNo={} userName={}",
                sessionId, userId,
                user != null ? user.getUserNo() : null,
                user != null ? user.getUserName() : null
        );

        service.enqueue(sessionId.trim());

        // ✅ 상담사 웹(WS)에 "새 대기 콜" push
        agentWsNotifier.notifyEnqueued(sessionId.trim());

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId.trim()));
    }
}
