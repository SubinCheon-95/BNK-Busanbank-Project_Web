package kr.co.busanbank.project.controller;


import kr.co.busanbank.project.service.HpService;
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

        log.info("code:{}", code);

        boolean result = hpService.verifyCode(code);

        Map<String,Boolean> resultMap = Map.of("isMatched",result);

        return ResponseEntity.ok(resultMap);
    }
}
