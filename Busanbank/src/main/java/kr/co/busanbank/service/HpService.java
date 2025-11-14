package kr.co.busanbank.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.SessionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class HpService {
    @Autowired
    private HttpSession session;

    private final SessionData sessionData;

    @Value("${spring.solapi.api.key}")
    private String apiKey;

    @Value("${spring.solapi.api.secret}")
    private String apiSecret;

    @Value("${spring.solapi.api.from}")
    private String fromPhoneNumber;


    // SMS 발송
    public void sendCode(String to) {
        DefaultMessageService solapiClient = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);

        String code = generateRandomNumber();

        try {
            Message message = new Message();
            message.setFrom(fromPhoneNumber);
            message.setTo(to.replace("-", ""));
            message.setText("BNK 부산은행 1차 프로젝트 이메일 인증코드는 [" + code + "] 입니다.");

            solapiClient.send(message);

            // 세션에 코드 저장
            sessionData.setCode(code);

        } catch (Exception e) {
            log.error("SMS 전송 실패", e);
            throw new RuntimeException("SMS 전송 실패");
        }
    }


    // 4자리 랜덤 숫자
    private String generateRandomNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }

    // 인증번호 검증
    public boolean verifyCode(String code){
        String sessCode = sessionData.getCode();
        return sessCode != null && sessCode.equals(code);
    }
}