package kr.co.busanbank.config;

import kr.co.busanbank.security.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESKeyConfig {

    @Value("${spring.aes.secret-key}")
    public void setKey(String key) {
        AESUtil.SECRET_KEY = key;
    }
}