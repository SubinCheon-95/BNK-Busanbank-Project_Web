package kr.co.busanbank.controller;

import kr.co.busanbank.service.HpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class HpController {

    private final HpService hpService;

    @PostMapping("/hp/code")
    public ResponseEntity<Map<String, Boolean>> verify(@RequestBody Map<String, String> jsonData){

        String code = jsonData.get("code");

        // 2025/12/16 - flutter용 인증 분리 - 작성자 : 오서정
        String mode = jsonData.get("mode"); // web(mode없으면 기존 웹 전송) / app
        String hp   = jsonData.get("hp");

        log.info("code:{}", code);

        // 2025/12/16 - flutter용 인증 분리 - 작성자 : 오서정
        boolean result;

        if ("app".equals(mode)) {
            result = hpService.verifyCodeForApp(hp, code);
        } else {
            result = hpService.verifyCode(code); // 기존 웹
        }


        Map<String,Boolean> resultMap = Map.of("isMatched",result);

        return ResponseEntity.ok(resultMap);
    }
}

