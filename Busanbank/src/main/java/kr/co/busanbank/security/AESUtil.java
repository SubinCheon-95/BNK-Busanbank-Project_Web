/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : aes-128 암호화/복호화 작성
*/
package kr.co.busanbank.security;


import lombok.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    //private static final String ALGORITHM = "AES";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // 고정 키 사용 (16자리: AES-128)
    public static String SECRET_KEY;


    //랜덤 IV 생성
    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // 암호화
    public static String encrypt(String data) throws Exception {
        byte[] iv = generateIV();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

        // 암호문 = IV(16바이트) + 암호데이터
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // 복호화
    public static String decrypt(String cipherText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(cipherText);

        // 앞 16바이트는 IV
        byte[] iv = new byte[16];
        System.arraycopy(decoded, 0, iv, 0, 16);

        // 나머지는 실제 암호문
        byte[] encrypted = new byte[decoded.length - 16];
        System.arraycopy(decoded, 16, encrypted, 0, encrypted.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] original = cipher.doFinal(encrypted);

        return new String(original, "UTF-8");
    }

    // 암호화
//    public static String encrypt(String data) throws Exception {
//        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
//        return Base64.getEncoder().encodeToString(encrypted);
//    }

    // 복호화
//    public static String decrypt(String encryptedData) throws Exception {
//        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//        byte[] decoded = Base64.getDecoder().decode(encryptedData);
//        byte[] original = cipher.doFinal(decoded);
//        return new String(original, "UTF-8");
//    }

    // 🔓 ECB 복호화 (옛날 DB 데이터용)
//    public static String decryptECB(String encryptedData) throws Exception {
//
//        // ECB는 IV 없음 → keySpec은 "AES" 만 사용
//        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
//
//        // ECB 전용 알고리즘
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//        byte[] decoded = Base64.getDecoder().decode(encryptedData);
//        byte[] original = cipher.doFinal(decoded);
//
//        return new String(original, "UTF-8");
//    }
}