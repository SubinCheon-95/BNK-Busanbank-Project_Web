/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : aes-128 암호화/복호화 작성
*/
package kr.co.busanbank.security;


import lombok.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";

    // 고정 키 사용 (16자리: AES-128)
    public static final String SECRET_KEY = "BusanBankAESKey1";
    //@Value("${spring.aes.secret}")
    //private String SECRET_KEY;

//    @Value("${aes.secret-key}")
//    public void setKey(String key) {
//        SECRET_KEY = key;   // static 변수에 주입
//    }

    // 암호화
    public static String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 복호화
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] original = cipher.doFinal(decoded);
        return new String(original, "UTF-8");
    }
}