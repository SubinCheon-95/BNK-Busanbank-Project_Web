package kr.co.busanbank.controller;

import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.ProductPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiProductPushController { //가입 완료 푸시 알림 - 작성자: 윤종인 2025.12.31
    private final ProductPushService  productPushService;

    @PostMapping("/productPush")
    public ResponseEntity<?> productPush(@RequestBody Map<String, String> data) {
        log.info("테스트 = {}", data.toString());

        try{
            int userNo = Integer.parseInt(data.get("userNo"));
            String productName = data.get("productName");

            String encryptedName = productPushService.findByUserName(userNo);
            log.info("encryptedName =  {}", encryptedName);

            String userName = "";
            if (encryptedName != null && !encryptedName.isEmpty()) {
                try {
                    userName = AESUtil.decrypt(encryptedName); // 복호화 실행
                } catch (Exception e) {
                    log.error("복호화 중 오류 발생: {}", e.getMessage());
                    userName = "고객"; // 복호화 실패 시 기본값
                }
            }
            log.info("복호화 userName = {}", userName);

            productPushService.insertProductPush(userNo, userName, productName);

            return ResponseEntity.ok("일부 테스트 성공");
        } catch (NumberFormatException e) {
            log.error("유저 번호 형식 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body("유저 번호는 숫자여야 합니다.");
        } catch (Exception e) {
            log.error("푸시 알림 처리 중 알 수 없는 서버 오류: ", e);
            return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다.");
        }
    }
}
