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

    private static final String TOKEN_CACHE_PREFIX = "chat:call:token:"; // chat:call:token:{sessionId}:{role}
    private static final String CHAT_SESSION_PREFIX = "chat:session:";   // hash key

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

        final String r = (role == null || role.isBlank()) ? "CUSTOMER" : role.trim().toUpperCase();
        final String cacheKey = TOKEN_CACHE_PREFIX + sessionId + ":" + r;

        // 1) 캐시 hit
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return objectMapper.readValue(cached, CallTokenResponse.class);
            }
        } catch (Exception e) {
            log.warn("⚠️ token cache read failed. key={}", cacheKey, e);
        }

        // 2) 채널 결정: chat:session:{sid}.agoraChannel 우선
        String channel = null;
        try {
            channel = redis.opsForHash().get(CHAT_SESSION_PREFIX + sessionId, "agoraChannel") != null
                    ? redis.opsForHash().get(CHAT_SESSION_PREFIX + sessionId, "agoraChannel").toString()
                    : null;
        } catch (Exception e) {
            log.warn("⚠️ read agoraChannel from chat session failed. sid={}", sessionId, e);
        }

        if (channel == null || channel.isBlank()) {
            // fallback 규칙(서버 표준을 voice_로)
            channel = "voice_" + sessionId;

            // (선택) 세션 hash에도 채널을 박아두면 이후 일관성↑
            try {
                redis.opsForHash().put(CHAT_SESSION_PREFIX + sessionId, "agoraChannel", channel);
            } catch (Exception e) {
                log.warn("⚠️ write agoraChannel to chat session failed. sid={}", sessionId, e);
            }
        }

        // 3) uid는 role별로 고정 발급(재접속 안정성)
        final int uid = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

        final String token = tokenService.createToken(channel, uid);
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(props.getTokenTtlSeconds());

        final CallTokenResponse issued = new CallTokenResponse(
                props.getAppId(),
                channel,
                uid,
                token,
                expiresAt
        );

        // 4) 캐시 저장
        try {
            String json = objectMapper.writeValueAsString(issued);
            redis.opsForValue().set(cacheKey, json, Duration.ofSeconds(props.getTokenTtlSeconds()));
        } catch (Exception e) {
            log.warn("⚠️ token cache write failed. key={}", cacheKey, e);
        }

        return issued;
    }
}
