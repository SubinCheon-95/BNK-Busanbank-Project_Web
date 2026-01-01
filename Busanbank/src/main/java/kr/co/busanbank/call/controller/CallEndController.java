package kr.co.busanbank.call.controller;


import kr.co.busanbank.call.service.CallEndService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/call")
public class CallEndController {

    private final CallEndService endService;

    public CallEndController(CallEndService endService) {
        this.endService = endService;
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> end(@PathVariable String sessionId,
                                 @RequestBody(required = false) EndRequest req,
                                 Authentication authentication) {

        String byAgentId = (authentication != null) ? authentication.getName() : "";
        String reason = (req == null) ? "" : req.getReason();

        endService.end(sessionId, byAgentId, reason);

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId));
    }

    @Data
    public static class EndRequest {
        private String reason;
    }
}
