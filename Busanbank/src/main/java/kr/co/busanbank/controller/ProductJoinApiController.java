package kr.co.busanbank.controller;

import kr.co.busanbank.dto.ProductJoinRequestDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.ProductJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/flutter/join")
@RequiredArgsConstructor
public class ProductJoinApiController {

    private final ProductJoinService productJoinService;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 🔥 Flutter 전용 상품 가입 API
     *
     * ✅ 웹과 완전히 분리됨 (웹에 영향 없음)
     * ✅ accountPasswordConfirm 없이도 작동
     * ✅ 강제 로그인 (userId=1, 김부산)
     */
    @PostMapping("/mock")
    public ResponseEntity<?> joinMock(@RequestBody ProductJoinRequestDTO joinRequest) {

        try {
            log.info("📱 [Flutter-MOCK] 상품 가입 요청 수신");
            log.info("   productNo      = {}", joinRequest.getProductNo());
            log.info("   principalAmount= {}", joinRequest.getPrincipalAmount());
            log.info("   contractTerm   = {}", joinRequest.getContractTerm());
            log.info("   accountPassword= {}", joinRequest.getAccountPassword());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 0. 강제 로그인 유저 (userId = "1" → 김부산)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String mockUserId = "1";
            Long userNo = memberMapper.findUserNoByUserId(mockUserId);
            log.info("🔍 [Flutter-MOCK] userNo 조회 완료 = {}", userNo);

            if (userNo == null) {
                log.error("❌ userId={} 에 해당하는 userNo를 찾을 수 없습니다.", mockUserId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("유저 정보를 찾을 수 없습니다.");
            }

            // USERPRODUCT.userId 컬럼에 들어갈 값
            joinRequest.setUserId(userNo.intValue());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1. 지점/직원 기본값 설정 (Flutter에서 아직 선택 없음)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (joinRequest.getBranchId() == null) {
                joinRequest.setBranchId(101);
            }
            if (joinRequest.getEmpId() == null) {
                joinRequest.setEmpId(1001);
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2. 계좌 비밀번호 검증 (Flutter는 확인 필드 없음)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String inputPassword = joinRequest.getAccountPassword();

            if (inputPassword == null || inputPassword.isEmpty()) {
                log.warn("❌ [Flutter-MOCK] 계좌 비밀번호가 null 또는 빈 문자열");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호를 입력해주세요.");
            }

            // 🔥 Flutter는 accountPasswordConfirm 없음
            // → 자동으로 같은 값으로 설정 (웹 로직과 호환)
            joinRequest.setAccountPasswordConfirm(inputPassword);
            log.info("📌 [Flutter-MOCK] accountPasswordConfirm 자동 설정 (같은 값)");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3. 원본 비밀번호 저장 (Service에서 AES 암호화용)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            joinRequest.setAccountPasswordOriginal(inputPassword);
            log.info("📌 [Flutter-MOCK] accountPasswordOriginal 설정 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. DB에서 계좌 비밀번호 조회 및 비교
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String dbPassword = memberMapper.findAccountPasswordByUserNo(userNo);
            log.info("🔍 [Flutter-MOCK] DB 비밀번호 조회 완료");
            log.info("   dbPassword   = {}", dbPassword);
            log.info("   inputPassword= {}", inputPassword);

            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ [Flutter-MOCK] DB에 계좌 비밀번호가 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 설정되지 않았습니다.");
            }

            boolean passwordMatches = false;

            log.info("📌 [Flutter-MOCK] 비밀번호 비교 시작 (BCrypt → AES → 평문)");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4-1. BCrypt 형식인지 확인
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (dbPassword.startsWith("$2a$") ||
                    dbPassword.startsWith("$2b$") ||
                    dbPassword.startsWith("$2y$")) {

                log.info("   → BCrypt 형식 감지");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);
                log.info("   → BCrypt 비교 결과: {}", passwordMatches);

            } else {
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 4-2. AES 또는 평문
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                try {
                    String decrypted = AESUtil.decrypt(dbPassword);
                    log.info("   → AES 복호화 성공");
                    log.info("   → decrypted   = {}", decrypted);
                    log.info("   → inputPassword= {}", inputPassword);

                    passwordMatches = inputPassword.equals(decrypted);
                    log.info("   → AES 비교 결과: {}", passwordMatches);

                } catch (Exception e) {
                    log.info("   → AES 복호화 실패, 평문으로 간주");
                    log.info("   → dbPassword   = {}", dbPassword);
                    log.info("   → inputPassword= {}", inputPassword);

                    passwordMatches = inputPassword.equals(dbPassword);
                    log.info("   → 평문 비교 결과: {}", passwordMatches);
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5. 비밀번호 불일치 시 종료
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (!passwordMatches) {
                log.warn("❌ [Flutter-MOCK] 계좌 비밀번호 불일치");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 일치하지 않습니다.");
            }

            log.info("✅ [Flutter-MOCK] 계좌 비밀번호 일치 확인 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 6. 실제 상품 가입 처리 (웹과 동일한 Service 사용)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            log.info("📌 [Flutter-MOCK] ProductJoinService.processJoin() 호출");
            boolean result = productJoinService.processJoin(joinRequest);

            if (!result) {
                log.error("❌ [Flutter-MOCK] 상품 가입 처리 실패 (Service에서 false 반환)");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("상품 가입 처리 중 오류가 발생했습니다.");
            }

            log.info("🎉 [Flutter-MOCK] 상품 가입 완료");
            return ResponseEntity.ok("상품 가입이 완료되었습니다.");

        } catch (Exception e) {
            log.error("❌ [Flutter-MOCK] 가입 처리 중 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
}