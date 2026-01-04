package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.service.VoiceCallQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/call/voice")
@RequiredArgsConstructor
public class ApiVoiceCallStatusController {

    private final VoiceCallQueueService service;

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<?> status(@PathVariable String sessionId) {

        // 1) WAITING?
        boolean waiting = service.isWaiting(sessionId); // 아래에 메서드 추가할 것

        if (waiting) {
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "sessionId", sessionId,
                    "status", "WAITING"
            ));
        }

        // 2) ACCEPTED? (active key 존재)
        String consultantId = service.getAcceptedConsultantId(sessionId); // 아래에 메서드 추가할 것
        if (consultantId != null && !consultantId.isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "sessionId", sessionId,
                    "status", "ACCEPTED",
                    "consultantId", consultantId
            ));
        }

        // 3) else ENDED
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "sessionId", sessionId,
                "status", "ENDED"
        ));
    }
}
