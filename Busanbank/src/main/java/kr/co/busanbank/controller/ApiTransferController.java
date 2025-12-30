package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.MemberService;
import kr.co.busanbank.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transfer")
public class ApiTransferController {


    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final MyService myService;

    // 2025/12/30 - 이체한도 조회 - 작성자: 오서정
    @GetMapping("/limit")
    public ResponseEntity<?> getTransferLimit(Authentication authentication) {
        UsersDTO user = (UsersDTO) authentication.getPrincipal();
        String userId = user.getUserId();

        int userNo = myService.findUserNo(userId);
        log.info("transfer-limit userNo = {}", userNo);

        UsersDTO freshUser = memberService.getUserLimitByUserNo(userNo);

        return ResponseEntity.ok(
                Map.of(
                        "onceLimit", freshUser.getOnceLimit(),
                        "dailyLimit", freshUser.getDailyLimit()
                )
        );
    }


    @PostMapping("/limit")
    public ResponseEntity<?> updateTransferLimit(@RequestBody Map<String, Long> req, Authentication authentication) {

        UsersDTO user = (UsersDTO) authentication.getPrincipal();
        String userId = user.getUserId();

        int userNo = myService.findUserNo(userId);

        Long onceLimit = req.get("onceLimit");
        Long dailyLimit = req.get("dailyLimit");

        memberService.updateTransferLimit(
                (long) userNo,
                onceLimit,
                dailyLimit
        );

        return ResponseEntity.ok().build();
    }


}
