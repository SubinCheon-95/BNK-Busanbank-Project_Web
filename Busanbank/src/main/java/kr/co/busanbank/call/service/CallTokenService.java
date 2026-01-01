package kr.co.busanbank.call.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.agora.AgoraProperties;
import kr.co.busanbank.agora.token.AgoraRtcTokenService;
import kr.co.busanbank.call.dto.CallTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class CallTokenService {

    private static final String TOKEN_CACHE_PREFIX = "chat:call:token:"; // key: chat:call:token:{sessionId}:{role}

    private final AgoraProperties props;
    private final AgoraRtcTokenService tokenService;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CallTokenService(AgoraProperties props,
                            AgoraRtcTokenService tokenService,
                            StringRedisTemplate redis,
                            ObjectMapper objectMapper) {
        this.props = props;
        this.tokenService = tokenService;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public CallTokenResponse issue(String sessionId, String role) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        String r = (role == null || role.isBlank()) ? "CUSTOMER" : role.trim();
        String cacheKey = TOKEN_CACHE_PREFIX + sessionId + ":" + r;

        // 1) 캐시 hit
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                CallTokenResponse resp = objectMapper.readValue(cached, CallTokenResponse.class);
                // resp.setCached(true);  // 아래 "cached 필드" 추가 시 사용
                return resp;
            }
        } catch (Exception e) {
            log.warn("⚠️ token cache read failed. key={}", cacheKey, e);
        }

        // 2) 캐시 miss -> 실제 발급
        String channel = "cs_" + sessionId;

        // ✅ 중요: 캐시할 거면 uid도 같이 고정되어야 하므로 “발급 시 1회 생성” 후 캐시에 저장
        int uid = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

        String token = tokenService.createToken(channel, uid);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(props.getTokenTtlSeconds());

        CallTokenResponse issued = new CallTokenResponse(
                props.getAppId(),
                channel,
                uid,
                token,
                expiresAt
        );
        // issued.setCached(false);

        // 3) 캐시 저장 (TTL=토큰 TTL)
        try {
            String json = objectMapper.writeValueAsString(issued);
            redis.opsForValue().set(cacheKey, json, Duration.ofSeconds(props.getTokenTtlSeconds()));
        } catch (Exception e) {
            log.warn("⚠️ token cache write failed. key={}", cacheKey, e);
        }

        return issued;
    }
}
