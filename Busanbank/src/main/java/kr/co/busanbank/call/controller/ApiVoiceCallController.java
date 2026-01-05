package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.CallStatus;
import kr.co.busanbank.call.service.CallCustomerWsNotifier;
import kr.co.busanbank.call.service.CallQueueKeys;
import kr.co.busanbank.call.service.CallWsAssignNotifier;
import kr.co.busanbank.call.service.VoiceCallQueueService;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/call/voice")
@RequiredArgsConstructor
public class ApiVoiceCallController {

    private final VoiceCallQueueService service;
    private final MemberMapper memberMapper;
    private final CallWsAssignNotifier agentWsNotifier; // enqueue 알림용
    private final CallCustomerWsNotifier customerWsNotifier; // (있다면) 고객 push

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

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

        String userId = authentication.getName();
        UsersDTO user = memberMapper.findByUserId(userId);

        log.info("[VOICE][API] enqueue sessionId={} userId={}", sessionId, userId);

        service.enqueue(sessionId.trim());
        agentWsNotifier.notifyEnqueued(sessionId.trim());

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId.trim()));
    }

}
