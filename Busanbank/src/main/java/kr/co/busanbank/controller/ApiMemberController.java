package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.mapper.MemberMapper;
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

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody UsersDTO dto) throws Exception {
        memberService.save(dto);
        return ResponseEntity.ok().build();
    }
}
