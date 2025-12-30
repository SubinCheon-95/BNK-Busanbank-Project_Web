package kr.co.busanbank.agora.token;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

public class AccessToken {
    // Agora AccessToken v006
    private static final String VERSION = "006";

    public enum Privilege {
        JOIN_CHANNEL(1),
        PUBLISH_AUDIO(2),
        PUBLISH_VIDEO(3),
        PUBLISH_DATA(4);

        public final short value;
        Privilege(int v) { this.value = (short) v; }
    }

    private final String appId;
    private final String appCertificate;
    private final String channelName;
    private final String uid; // MUST be string in signature/crc (Agora rule)

    private final int salt;
    private final int ts; // unix seconds (expiration timestamp)
    private final TreeMap<Short, Integer> privileges = new TreeMap<>();

    public AccessToken(String appId, String appCertificate, String channelName, String uid, int expireSeconds) {
        if (appId == null || appId.length() != 32) throw new IllegalArgumentException("Invalid appId");
        if (appCertificate == null || appCertificate.length() != 32) throw new IllegalArgumentException("Invalid appCertificate");
        this.appId = appId;
        this.appCertificate = appCertificate;
        this.channelName = channelName == null ? "" : channelName;
        this.uid = uid == null ? "" : uid;

        this.salt = new SecureRandom().nextInt(); // can be negative, OK (treated as uint32)
        int now = (int) (System.currentTimeMillis() / 1000L);
        this.ts = now + expireSeconds;
    }

    public void addPrivilege(Privilege p, int expireAtTs) {
        privileges.put(p.value, expireAtTs);
    }

    public String build() {
        // message = salt(uint32 LE) + ts(uint32 LE) + map
        byte[] message = packMessage();

        // signature = HMAC_SHA256(appCertificate, appId + channelName + uid + message)
        byte[] sig = hmacSha256(
                appCertificate.getBytes(StandardCharsets.UTF_8),
                concat(
                        appId.getBytes(StandardCharsets.UTF_8),
                        channelName.getBytes(StandardCharsets.UTF_8),
                        uid.getBytes(StandardCharsets.UTF_8),
                        message
                )
        );

        int crcChannel = crc32(channelName);
        int crcUid = crc32(uid);

        // content = sig + crcChannel + crcUid + message (all packed)
        byte[] content = packContent(sig, crcChannel, crcUid, message);

        return VERSION + appId + Base64.getEncoder().encodeToString(content);
    }

    private byte[] packMessage() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeUint32LE(bos, salt);
        writeUint32LE(bos, ts);
        writeMap(bos, privileges);
        return bos.toByteArray();
    }

    private static byte[] packContent(byte[] sig, int crcChannel, int crcUid, byte[] message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeUint16LE(bos, sig.length);
        writeBytes(bos, sig);
        writeUint32LE(bos, crcChannel);
        writeUint32LE(bos, crcUid);
        writeUint16LE(bos, message.length);
        writeBytes(bos, message);
        return bos.toByteArray();
    }

    private static void writeMap(ByteArrayOutputStream bos, Map<Short, Integer> map) {
        writeUint16LE(bos, map.size());
        for (Map.Entry<Short, Integer> e : map.entrySet()) {
            writeUint16LE(bos, e.getKey());
            writeUint32LE(bos, e.getValue());
        }
    }

    private static void writeUint16LE(ByteArrayOutputStream bos, int v) {
        byte[] b = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) v).array();
        writeBytes(bos, b);
    }

    private static void writeUint32LE(ByteArrayOutputStream bos, int v) {
        byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array();
        writeBytes(bos, b);
    }

    private static void writeBytes(ByteArrayOutputStream bos, byte[] b) {
        try { bos.write(b); } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    private static int crc32(String s) {
        CRC32 crc32 = new CRC32();
        crc32.update(s.getBytes(StandardCharsets.UTF_8));
        long v = crc32.getValue();
        return (int) v;
    }

    private static byte[] hmacSha256(byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] concat(byte[]... arrs) {
        int len = 0;
        for (byte[] a : arrs) len += a.length;
        byte[] out = new byte[len];
        int pos = 0;
        for (byte[] a : arrs) {
            System.arraycopy(a, 0, out, pos, a.length);
            pos += a.length;
        }
        return out;
    }

    public int getTs() { return ts; }
}
