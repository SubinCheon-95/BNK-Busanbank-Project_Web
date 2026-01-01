package kr.co.busanbank.call.controller;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/call")
public class CallTokenConsumedController {

    private static final String TOKEN_CONSUMED_FLAG_PREFIX = "chat:call:tokenConsumed:"; // {sessionId}:{role}

    private final StringRedisTemplate redis;

    @Value("${agora.token-ttl-seconds:3600}")
    private long tokenTtlSeconds;

    public CallTokenConsumedController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @PostMapping("/{sessionId}/token-consumed")
    public ResponseEntity<?> tokenConsumed(@PathVariable String sessionId,
                                           @RequestBody TokenConsumedRequest req) {

        String role = (req == null || req.getRole() == null || req.getRole().isBlank())
                ? "CUSTOMER"
                : req.getRole().trim();

        String key = TOKEN_CONSUMED_FLAG_PREFIX + sessionId + ":" + role;

        // join 성공했으니 "소비됨" 플래그 세팅
        redis.opsForValue().set(key, "1", Duration.ofSeconds(tokenTtlSeconds));

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId, "role", role));
    }

    @Data
    public static class TokenConsumedRequest {
        private String role;      // CUSTOMER / AGENT
        private String agoraUid;  // 선택(로그용)
    }
}
