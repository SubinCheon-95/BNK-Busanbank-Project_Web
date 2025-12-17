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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
            message.setText("딸깍은행 이메일 인증코드는 [" + code + "] 입니다.");

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

    /*
   날짜 : 2025/12/16
   내용 : 휴대폰 인증 flutter용(메모리 Map 기반)
   작성자 : 오서정
*/
    private final Map<String, String> appHpCodeMap = new ConcurrentHashMap<>();


    public void sendCodeForApp(String hp) {
        DefaultMessageService solapiClient =
                SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);

        String code = generateRandomNumber();

        try {
            Message message = new Message();
            message.setFrom(fromPhoneNumber);
            message.setTo(hp.replace("-", ""));
            message.setText("딸깍은행 인증코드는 [" + code + "] 입니다.");

            solapiClient.send(message);

            String normalizedHp = normalizeHp(hp);
            appHpCodeMap.put(normalizedHp, code); // 앱용 저장
            log.info("[APP] hp={}, code={}", hp, code);

        } catch (Exception e) {
            log.error("[APP] SMS 전송 실패", e);
            throw new RuntimeException("SMS 전송 실패");
        }
    }

    public boolean verifyCodeForApp(String hp, String code) {
        String normalizedHp = normalizeHp(hp);

        String saved = appHpCodeMap.get(normalizedHp);
        boolean result = saved != null && saved.equals(code);

        if (result) {
            appHpCodeMap.remove(normalizedHp);
        }
        return result;
    }

    private String normalizeHp(String hp) {
        return hp.replaceAll("-", "");
    }

}