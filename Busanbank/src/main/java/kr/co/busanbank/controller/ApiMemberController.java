package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.MemberService;
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


}