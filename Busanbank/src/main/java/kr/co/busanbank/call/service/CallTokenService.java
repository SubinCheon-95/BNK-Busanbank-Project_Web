package kr.co.busanbank.call.service;

import kr.co.busanbank.agora.AgoraProperties;
import kr.co.busanbank.agora.token.AgoraRtcTokenService;
import kr.co.busanbank.call.dto.CallTokenResponse;
import kr.co.busanbank.call.infra.AgoraTokenServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CallTokenService {

    private final AgoraProperties props;
    private final AgoraRtcTokenService tokenService;

    public CallTokenService(AgoraProperties props, AgoraRtcTokenService tokenService) {
        this.props = props;
        this.tokenService = tokenService;
    }

    public CallTokenResponse issue(String sessionId, String role) {

        String channel = "cs_" + sessionId;
        int uid = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

        String token = tokenService.createToken(channel, uid);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(props.getTokenTtlSeconds());

        return new CallTokenResponse(
                props.getAppId(),
                channel,
                uid,
                token,
                expiresAt
        );
    }
}
