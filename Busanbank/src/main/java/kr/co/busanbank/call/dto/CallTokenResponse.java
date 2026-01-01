package kr.co.busanbank.call.dto;

import java.time.OffsetDateTime;

public class CallTokenResponse {

    private String appId;
    private String channel;
    private int uid;
    private String token;
    private OffsetDateTime expiresAt;

    private boolean cached;

    public CallTokenResponse(
            String appId,
            String channel,
            int uid,
            String token,
            OffsetDateTime expiresAt
    ) {
        this.appId = appId;
        this.channel = channel;
        this.uid = uid;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getAppId() {
        return appId;
    }

    public String getChannel() {
        return channel;
    }

    public int getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}
