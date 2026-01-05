package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.HpService;
import kr.co.busanbank.service.MemberService;
import kr.co.busanbank.service.MyService;
import kr.co.busanbank.service.VisionOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class ApiMemberController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final HpService hpService;
    private final VisionOcrService visionOcrService;
    /**
     * Flutter 로그인 API
     * POST /api/member/login
     * ✅ JWT 토큰 생성 및 반환
     * ✅ userNo 포함
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@RequestBody Map<String, String> loginRequest) {

        String userId = loginRequest.get("userId");
        String userPw = loginRequest.get("userPw");

        log.info("📱 [Flutter] 로그인 요청 - userId: {}", userId);

        try {
            // 1. 사용자 조회
            UsersDTO user = memberMapper.findByUserId(userId);
            user.setUserName(AESUtil.decrypt(user.getUserName())); // 2026/01/02 - 조회 사용자 이름 복호화 - 작성자: 오서정
            //log.info("user = {}", user);

            if (user == null) {
                log.warn("❌ 사용자 없음 - userId: {}", userId);
                return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
            }

            // 2. 비밀번호 검증
            boolean passwordMatches = passwordEncoder.matches(userPw, user.getUserPw());

            if (!passwordMatches) {
                log.warn("❌ 비밀번호 불일치 - userId: {}", userId);
                return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
            }

            // 3. 회원 상태 확인
            if ("W".equals(user.getStatus())) {
                log.warn("❌ 탈퇴 진행중 - userId: {}", userId);
                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 진행중인 계정입니다"));
            }

            if ("S".equals(user.getStatus())) {
                log.warn("❌ 탈퇴 완료 - userId: {}", userId);
                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 완료된 계정입니다"));
            }

            // 4. JWT 토큰 생성
            String accessToken = jwtProvider.createToken(user, 1);  // 1일
            String refreshToken = jwtProvider.createToken(user, 7);  // 7일

            // 5. 응답 생성
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("userNo", user.getUserNo());  // ✅ userNo 추가!
            result.put("userId", user.getUserId());
            result.put("userName", user.getUserName()); // 2026/01/05 - userName 반환 추가 - 작성자: 오서정

            log.info("✅ [Flutter] 로그인 성공 - userId: {}, userNo: {}", userId, user.getUserNo());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ [Flutter] 로그인 처리 중 오류", e);
            return ResponseEntity.status(500).body(Map.of("error", "서버 오류"));
        }
    }

    @GetMapping("/terms")
    public ResponseEntity<List<TermDTO>> getTerms() {
        return ResponseEntity.ok(memberService.findTermsAll());
    }

    // 2025/12/18 - 회원가입 app 기능 연동 - 작성자: 오서정
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody UsersDTO dto) throws Exception {
        memberService.save(dto);
        return ResponseEntity.ok().build();
    }

    // 2025/12/18 - 아이디 찾기 app 기능 연동 - 작성자: 오서정
    @ResponseBody
    @PostMapping("/find/id/hp")
    public ResponseEntity<?> findUserIdByHp(@RequestBody Map<String, String> req) throws Exception {

        String userName = req.get("userName");
        String hp = req.get("hp");

        log.info("[APP] find id - userName={}, hp={}", userName, hp);

        UsersDTO user = memberService.getUserIdInfoHp(userName, hp);

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "회원정보가 일치하지 않습니다."));
        }

        user.setUserName(AESUtil.decrypt(user.getUserName()));
        user.setHp(AESUtil.decrypt(user.getHp()));

        return ResponseEntity.ok(
            Map.of(
                "userId", user.getUserId(),
                "userName", user.getUserName()
            )
        );
    }

    // 2025/12/18 - 비밀번호 찾기(재설정) app 기능 연동 - 작성자: 오서정
    @PostMapping("/find/pw/hp")
    public ResponseEntity<?> verifyUserForPw(@RequestBody Map<String,String> req) throws Exception {

        UsersDTO user = memberService.getUserPwInfoHp(
                req.get("userName"),
                req.get("userId"),
                req.get("hp")
        );

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "회원정보가 일치하지 않습니다."));
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/find/pw/reset")
    public ResponseEntity<?> resetPw(@RequestBody Map<String,String> req) {

        memberService.modifyPw(
                req.get("userId"),
                req.get("userPw")
        );

        return ResponseEntity.ok().build();
    }


    // 2025/12/21 - 간편 로그인 flutter 연동 - 작성자: 오서정
    @PostMapping("/simple-login")
    public ResponseEntity<?> simpleLogin(@RequestBody Map<String, String> body) {

        String userId = body.get("userId");
        log.info("📱 [Flutter] 간편 로그인 요청 - userId: {}", userId);

        try {
            UsersDTO user = memberMapper.findByUserId(userId);
            user.setUserName(AESUtil.decrypt(user.getUserName())); // 2026/01/02 - 조회 사용자 이름 복호화 - 작성자: 오서정
            //log.info("user = {}", user);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
            }

            if ("W".equals(user.getStatus())) {
                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 진행중인 계정입니다"));
            }
            if ("S".equals(user.getStatus())) {
                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 완료된 계정입니다"));
            }

            return ResponseEntity.ok(buildLoginResponse(user));

        } catch (Exception e) {
            log.error("❌ [Flutter] 간편 로그인 처리 중 오류", e);
            return ResponseEntity.status(500).body(Map.of("error", "서버 오류"));
        }
    }


    private Map<String, Object> buildLoginResponse(UsersDTO user) {

        String accessToken = jwtProvider.createToken(user, 1);   // 1일
        String refreshToken = jwtProvider.createToken(user, 7);  // 7일

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userNo", user.getUserNo());
        result.put("userId", user.getUserId());
        result.put("userName", user.getUserName());
        result.put("role", user.getRole());



        return result;
    }



    // 2026/01/01 - 신분증 OCR(Vision API) - 작성자: 오서정
    @PostMapping("/id-ocr")
    public ResponseEntity<?> idOcr(@RequestBody Map<String, String> req) {

        String base64 = req.get("base64");
        if (base64 == null || base64.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "base64 누락"));
        }

        String text = visionOcrService.detectText(base64);
        log.info("vision text={}", text);
        return ResponseEntity.ok(Map.of(
                "text", text
        ));
    }

    // ✅ 요청 DTO (내부 클래스 or 별도 파일로 빼도 됨)
    static class IdVerifyRequest {
        public String userName;
        public String rrn;
    }
    private final MyService myService;
    @PostMapping("/id-verify")
    public ResponseEntity<?> verifyId(@RequestBody IdVerifyRequest req, Authentication authentication) {

        UsersDTO user = (UsersDTO) authentication.getPrincipal();
        String userId = user.getUserId();

        int userNo = myService.findUserNo(userId);
        log.info("id-verify userNo = {}", userNo);


        if (req == null || req.userName == null || req.rrn == null ||
                req.userName.isBlank() || req.rrn.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "userName/rrn 누락"));
        }

        boolean matched = memberService.verifyIdInfo(userNo, req.userName, req.rrn);

        // ⚠️ 로그에 주민번호/이름 평문 찍지 말기
        log.info("id-verify result userNo={}, matched={}", userNo, matched);

        return ResponseEntity.ok(Map.of(
                "matched", matched
        ));
    }

    // 2026/01/02 - otp 발급 휴대폰인증 구현 - 작성자: 오서정
    static class OtpHpSendRequest {
        public String hp;
    }

    static class OtpHpVerifyRequest {
        public String hp;
        public String code;
    }


    @PostMapping("/otp/hp/send")
    public ResponseEntity<?> sendOtpHpCode(@RequestBody OtpHpSendRequest req,
                                           Authentication authentication) {

        if (req == null || req.hp == null || req.hp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "hp 누락"));
        }

        UsersDTO principal = (UsersDTO) authentication.getPrincipal();
        String userId = principal.getUserId();

        int userNo = myService.findUserNo(userId);

        // ✅ 1) 로그인 사용자 휴대폰과 일치하는지 확인(복호화 비교)
        boolean matched = memberService.verifyHpInfo(userNo, req.hp);
        if (!matched) {
            // ⚠️ hp 평문 로그 찍지 말기
            return ResponseEntity.status(403).body(Map.of("message", "로그인 사용자 휴대폰 번호와 일치하지 않습니다."));
        }

        // ✅ 2) 일치하면 SMS 발송(앱용 Map 저장)
        //    HpService는 하이픈 제거해서 저장하니 req.hp 그대로 넣어도 됨
        hpService.sendCodeForApp(req.hp);

        return ResponseEntity.ok(Map.of("message", "인증 코드 발송 완료"));
    }

    @PostMapping("/otp/hp/verify")
    public ResponseEntity<?> verifyOtpHpCode(@RequestBody OtpHpVerifyRequest req,
                                             Authentication authentication) {

        if (req == null || req.hp == null || req.code == null ||
                req.hp.isBlank() || req.code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "hp/code 누락"));
        }

        UsersDTO principal = (UsersDTO) authentication.getPrincipal();
        String userId = principal.getUserId();

        int userNo = myService.findUserNo(userId);

        // ✅ 1) 먼저 hp가 내 번호가 맞는지 검증
        boolean matched = memberService.verifyHpInfo(userNo, req.hp);
        if (!matched) {
            return ResponseEntity.status(403).body(Map.of("isMatched", false));
        }

        // ✅ 2) 코드 검증
        boolean ok = hpService.verifyCodeForApp(req.hp, req.code);

        return ResponseEntity.ok(Map.of("isMatched", ok));
    }

}