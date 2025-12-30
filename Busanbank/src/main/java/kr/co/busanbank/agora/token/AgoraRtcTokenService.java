package kr.co.busanbank.agora.token;

import kr.co.busanbank.agora.AgoraProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgoraRtcTokenService {

    private final AgoraProperties props;

    public AgoraRtcTokenService(AgoraProperties props) {
        this.props = props;
    }

    public String createToken(String channel, int uid) {
        // ✅ Agora 토큰은 "expireAt(UNIX seconds)"를 privileges에 넣는 방식
        return RtcTokenBuilder.buildTokenWithUid(
                props.getAppId(),
                props.getAppCertificate(),
                channel,
                uid,
                RtcTokenBuilder.Role.PUBLISHER,
                props.getTokenTtlSeconds()
        );
    }
}
