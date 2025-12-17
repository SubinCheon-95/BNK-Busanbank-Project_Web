package kr.co.busanbank.controller;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.mapper.*;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🔥 Flutter 전용 통합 API 컨트롤러
 * 웹과 분리된 Flutter 전용 엔드포인트
 * - 지점 목록
 * - 직원 목록
 * - 약관 조회
 * - 쿠폰 조회
 * - 포인트 조회
 * - 상품 가입
 * 작성일: 2025-12-11
 * 작성자: Claude + 샬
 */
@Slf4j
@RestController
@RequestMapping("/api/flutter")
@RequiredArgsConstructor
public class FlutterApiController {

    // Mapper
    private final BranchMapper branchMapper;
    private final EmployeeMapper employeeMapper;
    private final UserCouponMapper userCouponMapper;
    private final MemberMapper memberMapper;
    private final MyMapper myMapper;
    private final PointMapper pointMapper;
    private final AttendanceMapper attendanceMapper;
    private final BranchCheckinMapper branchCheckinMapper;

    // Service
    private final ProductTermsService productTermsService;
    private final ProductJoinService productJoinService;
    private final PasswordEncoder passwordEncoder;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. 지점 목록 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 전체 지점 목록 조회
     *
     * GET /api/flutter/branches
     *
     * Response:
     * [
     *   {
     *     "branchId": 1,
     *     "branchName": "본점",
     *     "branchAddr": "부산시 중구",
     *     "branchTel": "051-123-4567"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/branches")
    public ResponseEntity<List<BranchDTO>> getBranches() {
        try {
            log.info("📱 [Flutter] 지점 목록 조회");
            List<BranchDTO> branches = branchMapper.selectAllBranches();
            log.info("✅ 지점 {}개 조회 완료", branches.size());
            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            log.error("❌ 지점 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. 직원 목록 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 지점별 직원 목록 조회
     *
     * GET /api/flutter/employees?branchId=1
     *
     * Response:
     * [
     *   {
     *     "empId": 1,
     *     "empName": "김행원",
     *     "branchId": 1,
     *     "empPosition": "대리"
     *   },
     *   ...
     * ]
     */

    /**
     * 지점별 직원 목록 조회 (Flutter 전용)
     * GET /api/flutter/branches/{branchId}/employees
     */
    @GetMapping("/branches/{branchId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByBranch(
            @PathVariable Integer branchId) {
        try {
            log.info("📱 [Flutter] 지점별 직원 조회 - branchId: {}", branchId);
            List<EmployeeDTO> employees = employeeMapper.selectEmployeesByBranch(branchId);
            log.info("✅ 직원 {}명 조회 완료", employees.size());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("❌ 직원 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployees(
            @RequestParam(required = false) Integer branchId) {
        try {
            log.info("📱 [Flutter] 직원 목록 조회 - branchId: {}", branchId);

            List<EmployeeDTO> employees;
            if (branchId != null) {
                employees = employeeMapper.selectEmployeesByBranch(branchId);
            } else {
                employees = employeeMapper.selectAllEmployees();
            }

            log.info("✅ 직원 {}명 조회 완료", employees.size());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("❌ 직원 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. 약관 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 상품별 약관 조회
     *
     * GET /api/flutter/products/{productNo}/terms
     *
     * Response:
     * [
     *   {
     *     "termsId": 1,
     *     "productNo": 402,
     *     "termsTitle": "예금거래 기본약관",
     *     "termsContent": "제1조...",
     *     "isRequired": true
     *   },
     *   ...
     * ]
     */
    @GetMapping("/products/{productNo}/terms")
    public ResponseEntity<List<ProductTermsDTO>> getTerms(
            @PathVariable int productNo) {
        try {
            log.info("📱 [Flutter] 약관 조회 - productNo: {}", productNo);
            List<ProductTermsDTO> terms = productTermsService.getTermsByProductNo(productNo);
            log.info("✅ 약관 {}개 조회 완료", terms.size());
            return ResponseEntity.ok(terms);
        } catch (Exception e) {
            log.error("❌ 약관 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. 쿠폰 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 사용자 쿠폰 조회 (사용 가능한 것만)
     *
     * GET /api/flutter/coupons/user/{userNo}
     *
     * Response:
     * [
     *   {
     *     "couponId": 1,
     *     "couponName": "신규 가입 쿠폰",
     *     "bonusRate": 0.5,
     *     "isUsed": false,
     *     "expiryDate": "2025-12-31"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/coupons/user/{userNo}")
    public ResponseEntity<List<UserCouponDTO>> getUserCoupons(
            @PathVariable Long userNo) {
        try {
            log.info("📱 [Flutter] 쿠폰 조회 - userNo: {}", userNo);
            List<UserCouponDTO> coupons = userCouponMapper.selectAvailableCoupons(userNo);
            log.info("✅ 쿠폰 {}개 조회 완료", coupons.size());
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            log.error("❌ 쿠폰 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 5. 포인트 조회
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 사용자 포인트 조회
     *
     * GET /api/flutter/points/user/{userNo}
     *
     * Response:
     * {
     *   "userNo": 231837269,
     *   "totalPoints": 1500,
     *   "availablePoints": 1200,
     *   "usedPoints": 300
     * }
     */
    @GetMapping("/points/user/{userNo}")
    public ResponseEntity<?> getUserPoints(@PathVariable Long userNo) {
        try {
            log.info("📱 [Flutter] 포인트 조회 - userNo: {}", userNo);

            //  포인트 조회
            Integer totalPoints = pointMapper.selectUserPoints(userNo);

            if (totalPoints == null) {
                totalPoints = 0;
            }

            // 간단한 JSON 응답
            var response = new java.util.HashMap<String, Object>();
            response.put("userNo", userNo);
            response.put("totalPoints", totalPoints);
            response.put("availablePoints", totalPoints);
            response.put("usedPoints", 0);

            log.info("✅ 포인트 조회 완료: {}P", totalPoints);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 포인트 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 6. 상품 가입 (게스트 - 로그인 전)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 🔥 게스트 상품 가입 (로그인 전 - 김부산 고정)
     *
     * POST /api/flutter/join/guest
     *
     * Request Body:
     * {
     *   "productNo": 402,
     *   "principalAmount": 1000000,
     *   "contractTerm": 12,
     *   "branchId": 1,
     *   "empId": 1,
     *   "accountPassword": "1111",
     *   "agreedTermIds": [1, 2],
     *   "usedPoints": 0,
     *   "selectedCouponId": null,
     *   ...
     * }
     *
     * Response:
     * "상품 가입이 완료되었습니다."
     */
    @PostMapping("/join/guest")
    public ResponseEntity<?> joinAsGuest(@RequestBody ProductJoinRequestDTO joinRequest) {

        try {
            log.info("📱 [Flutter-GUEST] 상품 가입 요청 수신");
            log.info("   productNo      = {}", joinRequest.getProductNo());
            log.info("   principalAmount= {}", joinRequest.getPrincipalAmount());
            log.info("   contractTerm   = {}", joinRequest.getContractTerm());
            log.info("   branchId       = {}", joinRequest.getBranchId());
            log.info("   empId          = {}", joinRequest.getEmpId());
            log.info("   usedPoints     = {}", joinRequest.getUsedPoints());
            log.info("   couponId       = {}", joinRequest.getSelectedCouponId());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1. 강제 로그인 (userId = "1" → 김부산)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String mockUserId = "1";
            Long userNo = memberMapper.findUserNoByUserId(mockUserId);
            log.info("🔍 [Flutter-GUEST] userNo 조회 완료 = {}", userNo);

            if (userNo == null) {
                log.error("❌ userId={} 에 해당하는 userNo를 찾을 수 없습니다.", mockUserId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("유저 정보를 찾을 수 없습니다.");
            }

            joinRequest.setUserId(userNo.intValue());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2. 지점/직원 검증 (필수!)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (joinRequest.getBranchId() == null) {
                log.warn("❌ [Flutter-GUEST] branchId 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("지점을 선택해주세요.");
            }

            if (joinRequest.getEmpId() == null) {
                log.warn("❌ [Flutter-GUEST] empId 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("담당자를 선택해주세요.");
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3. 계좌 비밀번호 처리
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String inputPassword = joinRequest.getAccountPassword();

            if (inputPassword == null || inputPassword.isEmpty()) {
                log.warn("❌ [Flutter-GUEST] 계좌 비밀번호 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호를 입력해주세요.");
            }

            // Flutter는 confirm 없음 → 자동 설정
            joinRequest.setAccountPasswordConfirm(inputPassword);
            joinRequest.setAccountPasswordOriginal(inputPassword);
            log.info("📌 [Flutter-GUEST] accountPasswordConfirm 자동 설정 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. DB 비밀번호 확인
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String dbPassword = memberMapper.findAccountPasswordByUserNo(userNo);
            log.info("🔍 [Flutter-GUEST] DB 비밀번호 조회 완료");

            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ [Flutter-GUEST] DB에 계좌 비밀번호가 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 설정되지 않았습니다.");
            }

            boolean passwordMatches = false;

            log.info("📌 [Flutter-GUEST] 비밀번호 비교 시작 (BCrypt → AES → 평문)");

            // BCrypt 확인
            if (dbPassword.startsWith("$2a$") ||
                    dbPassword.startsWith("$2b$") ||
                    dbPassword.startsWith("$2y$")) {

                log.info("   → BCrypt 형식 감지");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);
                log.info("   → BCrypt 비교 결과: {}", passwordMatches);

            } else {
                // AES 또는 평문
                try {
                    String decrypted = AESUtil.decrypt(dbPassword);
                    log.info("   → AES 복호화 성공");
                    passwordMatches = inputPassword.equals(decrypted);
                    log.info("   → AES 비교 결과: {}", passwordMatches);
                } catch (Exception e) {
                    log.info("   → AES 복호화 실패, 평문으로 간주");
                    passwordMatches = inputPassword.equals(dbPassword);
                    log.info("   → 평문 비교 결과: {}", passwordMatches);
                }
            }

            if (!passwordMatches) {
                log.warn("❌ [Flutter-GUEST] 계좌 비밀번호 불일치");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 일치하지 않습니다.");
            }

            log.info("✅ [Flutter-GUEST] 계좌 비밀번호 일치 확인 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5. 실제 상품 가입 처리 (웹과 동일)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            log.info("📌 [Flutter-GUEST] ProductJoinService.processJoin() 호출");
            boolean result = productJoinService.processJoin(joinRequest);

            if (!result) {
                log.error("❌ [Flutter-GUEST] 상품 가입 처리 실패");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("상품 가입 처리 중 오류가 발생했습니다.");
            }

            log.info("🎉 [Flutter-GUEST] 상품 가입 완료");
            return ResponseEntity.ok("상품 가입이 완료되었습니다.");

        } catch (Exception e) {
            log.error("❌ [Flutter-GUEST] 가입 처리 중 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 7. 상품 가입 (인증 - 로그인 후)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 🔥 인증 상품 가입 (로그인 후 - 실제 사용자)
     * POST /api/flutter/join/auth
     * ✅ JWT에서 실제 로그인한 userId 추출
     * ✅ mock처럼 완벽한 검증 로직
     * ✅ 웹과 완전히 분리
     */
    @PostMapping("/join/auth")
    public ResponseEntity<?> joinAsAuth(
            @RequestBody ProductJoinRequestDTO joinRequest,
            Authentication authentication
    ) {
        try {
            log.info("📱 [Flutter-AUTH] 인증 가입 요청 수신");
            log.info("   productNo      = {}", joinRequest.getProductNo());
            log.info("   principalAmount= {}", joinRequest.getPrincipalAmount());
            log.info("   contractTerm   = {}", joinRequest.getContractTerm());
            log.info("   accountPassword= {}", joinRequest.getAccountPassword());
            log.info("   usedPoints     = {}", joinRequest.getUsedPoints());
            log.info("   selectedCouponId= {}", joinRequest.getSelectedCouponId());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 0. JWT에서 userId 추출 (✅ mock과 다른 부분!)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("❌ [Flutter-AUTH] 인증되지 않은 요청");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("로그인이 필요합니다.");
            }

            String userId = authentication.getName();
            log.info("🔑 [Flutter-AUTH] 인증된 userId: {}", userId);

            Long userNo = memberMapper.findUserNoByUserId(userId);
            log.info("🔍 [Flutter-AUTH] userNo 조회 완료 = {}", userNo);

            if (userNo == null) {
                log.error("❌ userId={} 에 해당하는 userNo를 찾을 수 없습니다.", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("사용자 정보를 찾을 수 없습니다.");
            }

            // USERPRODUCT.userId 컬럼에 들어갈 값
            joinRequest.setUserId(userNo.intValue());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1. 지점/직원 검증 (✅ mock과 동일!)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            if (joinRequest.getBranchId() == null) {
                log.warn("❌ [Flutter-AUTH] branchId 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("지점을 선택해주세요.");
            }

            if (joinRequest.getEmpId() == null) {
                log.warn("❌ [Flutter-AUTH] empId 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("담당자를 선택해주세요.");
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2. 계좌 비밀번호 검증 (✅ mock과 동일!)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String inputPassword = joinRequest.getAccountPassword();

            if (inputPassword == null || inputPassword.isEmpty()) {
                log.warn("❌ [Flutter-AUTH] 계좌 비밀번호가 null 또는 빈 문자열");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호를 입력해주세요.");
            }

            // 🔥 Flutter는 accountPasswordConfirm 없음
            // → 자동으로 같은 값으로 설정 (웹 로직과 호환)
            joinRequest.setAccountPasswordConfirm(inputPassword);
            log.info("📌 [Flutter-AUTH] accountPasswordConfirm 자동 설정 (같은 값)");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3. 원본 비밀번호 저장 (Service에서 AES 암호화용)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            joinRequest.setAccountPasswordOriginal(inputPassword);
            log.info("📌 [Flutter-AUTH] accountPasswordOriginal 설정 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. DB에서 계좌 비밀번호 조회 및 비교
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            String dbPassword = memberMapper.findAccountPasswordByUserNo(userNo);
            log.info("🔍 [Flutter-AUTH] DB 비밀번호 조회 완료");
            log.info("   dbPassword   = {}", dbPassword);
            log.info("   inputPassword= {}", inputPassword);

            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ [Flutter-AUTH] DB에 계좌 비밀번호가 없음");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 설정되지 않았습니다.");
            }

            boolean passwordMatches = false;

            log.info("📌 [Flutter-AUTH] 비밀번호 비교 시작 (BCrypt → AES → 평문)");

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
                log.warn("❌ [Flutter-AUTH] 계좌 비밀번호 불일치");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("계좌 비밀번호가 일치하지 않습니다.");
            }

            log.info("✅ [Flutter-AUTH] 계좌 비밀번호 일치 확인 완료");

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 6. 실제 상품 가입 처리 (웹과 동일한 Service 사용)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            log.info("📌 [Flutter-AUTH] ProductJoinService.processJoin() 호출");
            boolean result = productJoinService.processJoin(joinRequest);

            if (!result) {
                log.error("❌ [Flutter-AUTH] 상품 가입 처리 실패 (Service에서 false 반환)");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("상품 가입 처리 중 오류가 발생했습니다.");
            }

            log.info("🎉 [Flutter-AUTH] 상품 가입 완료!");
            log.info("   userId: {}, userNo: {}", userId, userNo);
            log.info("   productNo: {}", joinRequest.getProductNo());
            log.info("   principalAmount: {}", joinRequest.getPrincipalAmount());
            log.info("   contractTerm: {}", joinRequest.getContractTerm());
            log.info("   usedPoints: {}", joinRequest.getUsedPoints());
            log.info("   selectedCouponId: {}", joinRequest.getSelectedCouponId());

            return ResponseEntity.ok("상품 가입이 완료되었습니다.");

        } catch (Exception e) {
            log.error("❌ [Flutter-AUTH] 가입 처리 중 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }


    }

    /**
     * 🔥 계좌 비밀번호 검증 API
     * POST /api/flutter/verify/account-password
     * ✅ STEP 2에서 계좌 비밀번호 검증용
     */
    @PostMapping("/verify/account-password")
    public ResponseEntity<?> verifyAccountPassword(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        try {
            log.info("📱 [Flutter] 계좌 비밀번호 검증 요청");

            // 1. JWT에서 userId 추출
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            String userId = authentication.getName();
            log.info("🔑 [Flutter] 인증된 userId: {}", userId);

            // 2. userNo 조회
            Long userNo = memberMapper.findUserNoByUserId(userId);

            if (userNo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "사용자 정보를 찾을 수 없습니다."));
            }

            // 3. 요청에서 입력 비밀번호 추출
            String inputPassword = (String) request.get("accountPassword");

            if (inputPassword == null || inputPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "계좌 비밀번호를 입력해주세요."));
            }

            // 4. DB에서 계좌 비밀번호 조회
            String dbPassword = memberMapper.findAccountPasswordByUserNo(userNo);

            if (dbPassword == null || dbPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "계좌 비밀번호가 설정되지 않았습니다."));
            }

            // 5. 비밀번호 비교 (BCrypt → AES → 평문)
            boolean passwordMatches = false;

            if (dbPassword.startsWith("$2a$") ||
                    dbPassword.startsWith("$2b$") ||
                    dbPassword.startsWith("$2y$")) {

                log.info("   → BCrypt 형식 감지");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);

            } else {
                try {
                    String decrypted = AESUtil.decrypt(dbPassword);
                    passwordMatches = inputPassword.equals(decrypted);
                } catch (Exception e) {
                    passwordMatches = inputPassword.equals(dbPassword);
                }
            }

            if (passwordMatches) {
                log.info("✅ [Flutter] 계좌 비밀번호 일치");
                return ResponseEntity.ok(Map.of("success", true, "message", "계좌 비밀번호가 확인되었습니다."));
            } else {
                log.warn("❌ [Flutter] 계좌 비밀번호 불일치");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "계좌 비밀번호가 일치하지 않습니다."));
            }

        } catch (Exception e) {
            log.error("❌ [Flutter] 계좌 비밀번호 검증 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

}