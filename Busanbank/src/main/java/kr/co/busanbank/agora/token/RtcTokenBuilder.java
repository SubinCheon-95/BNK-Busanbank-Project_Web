package kr.co.busanbank.agora.token;

public class RtcTokenBuilder {

    public enum Role {
        PUBLISHER(1),
        SUBSCRIBER(2);

        public final int value;
        Role(int v) { this.value = v; }
    }

    public static String buildTokenWithUid(
            String appId,
            String appCertificate,
            String channelName,
            int uid,
            Role role,
            int expireSeconds
    ) {
        String uidStr = String.valueOf(uid); // Agora token signature uses uid as string
        AccessToken token = new AccessToken(appId, appCertificate, channelName, uidStr, expireSeconds);

        // privileges expire at token.ts
        int expireAt = token.getTs();
        token.addPrivilege(AccessToken.Privilege.JOIN_CHANNEL, expireAt);

        if (role == Role.PUBLISHER) {
            token.addPrivilege(AccessToken.Privilege.PUBLISH_AUDIO, expireAt);
            token.addPrivilege(AccessToken.Privilege.PUBLISH_VIDEO, expireAt);
            token.addPrivilege(AccessToken.Privilege.PUBLISH_DATA, expireAt);
        }

        return token.build();
    }
}
