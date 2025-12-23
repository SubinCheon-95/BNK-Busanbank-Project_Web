package kr.co.busanbank.controller;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.mapper.*;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
 * 작성자: 수진
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
    private final AttendanceService attendanceService;
    private final BranchCheckinService branchCheckinService;
    private final PointService pointService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private NewsCrawlerService newsCrawlerService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;


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
            // ✅ selectAvailableCoupons 쿼리 사용 (ucNo 필드 매핑이 올바름)
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

            // ✅ UsersDTO에서 userId 추출!
            Object principal = authentication.getPrincipal();
            String userId;

            if (principal instanceof UsersDTO) {
                userId = ((UsersDTO) principal).getUserId();
                log.info("🔑 [Flutter] 인증된 userId: {}", userId);
            } else {
                userId = authentication.getName();
            }

            Long userNo = memberMapper.findUserNoByUserId(userId);
            log.info("🔍 [Flutter] userNo 조회: {}", userNo);

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

            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "message", "상품 가입이 완료되었습니다."
                )
            );

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

            // ✅ UsersDTO에서 userId 추출
            Object principal = authentication.getPrincipal();
            String userId;

            if (principal instanceof UsersDTO) {
                userId = ((UsersDTO) principal).getUserId();
                log.info("🔑 [Flutter] 인증된 userId: {}", userId);
            } else {
                userId = authentication.getName();
                log.info("🔑 [Flutter] 인증된 userId (fallback): {}", userId);
            }

            // 2. userNo 조회
            Long userNo = memberMapper.findUserNoByUserId(userId);
            log.info("🔍 [Flutter] userNo 조회 완료: {}", userNo);

            if (userNo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "사용자 정보를 찾을 수 없습니다."));
            }

            // 3. 요청에서 입력 비밀번호 추출
            String inputPassword = (String) request.get("accountPassword");
            log.info("📌 [Flutter] 입력된 비밀번호: {}", inputPassword);

            if (inputPassword == null || inputPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "계좌 비밀번호를 입력해주세요."));
            }

            // 4. DB에서 계좌 비밀번호 조회
            String dbPassword = memberMapper.findAccountPasswordByUserNo(userNo);
            log.info("🔍 [Flutter] DB 비밀번호 조회 완료");
            log.info("   dbPassword: {}", dbPassword);

            if (dbPassword == null || dbPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "계좌 비밀번호가 설정되지 않았습니다."));
            }

            // 5. 비밀번호 비교 (BCrypt → AES → 평문)
            boolean passwordMatches = false;

            log.info("📌 [Flutter] 비밀번호 비교 시작");

            if (dbPassword.startsWith("$2a$") ||
                    dbPassword.startsWith("$2b$") ||
                    dbPassword.startsWith("$2y$")) {

                log.info("   → BCrypt 형식 감지");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);
                log.info("   → BCrypt 비교 결과: {}", passwordMatches);

            } else {
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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 8. 출석체크 API (인증 필요)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 출석체크 현황 조회
     * GET /api/flutter/attendance/status/{userNo}
     *
     * Response:
     * {
     *   "isCheckedToday": true,
     *   "consecutiveDays": 5,
     *   "totalPoints": 2500,
     *   "weeklyAttendance": [true, true, false, true, true, false, false]
     * }
     */
    @GetMapping("/attendance/status/{userNo}")
    public ResponseEntity<Map<String, Object>> getAttendanceStatus(
            @PathVariable Long userNo,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 출석체크 현황 조회 - userNo: {}", userNo);

            // 인증 확인 (옵션)
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            int userId = userNo.intValue();

            boolean isCheckedToday = attendanceService.isAttendedToday(userId);
            int consecutiveDays = attendanceService.getCurrentConsecutiveDays(userId);
            Integer totalPoints = pointMapper.selectUserPoints(userNo);

            Map<String, Object> response = new HashMap<>();
            response.put("isCheckedToday", isCheckedToday);
            response.put("consecutiveDays", consecutiveDays);
            response.put("totalPoints", totalPoints != null ? totalPoints : 0);

            // 주간 출석 현황 (최근 7일)
            // TODO: 실제로는 Service에서 구현 필요
            response.put("weeklyAttendance", new boolean[]{false, false, false, false, false, false, false});

            log.info("✅ 출석체크 현황 조회 완료 - 오늘출석: {}, 연속: {}일", isCheckedToday, consecutiveDays);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 출석체크 현황 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "출석체크 현황 조회 실패"));
        }
    }

    /**
     * 출석체크 수행
     * POST /api/flutter/attendance/check
     *
     * Request Body:
     * {
     *   "userId": 231837269
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "earnedPoints": 10,
     *   "consecutiveDays": 6,
     *   "bonusPoints": 0,
     *   "message": "출석체크 완료!"
     * }
     */
    @PostMapping("/attendance/check")
    public ResponseEntity<Map<String, Object>> checkAttendance(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 출석체크 요청 - request: {}", request);

            // 인증 확인 (옵션)
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            Integer userId = (Integer) request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "userId가 필요합니다."));
            }

            Map<String, Object> result = attendanceService.checkAttendance(userId);

            log.info("✅ 출석체크 완료 - userId: {}, result: {}", userId, result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 출석체크 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "출석체크 처리 중 오류가 발생했습니다."));
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 9. 영업점 체크인 API (인증 필요)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 영업점 체크인 이력 조회
     * GET /api/flutter/checkin/history/{userNo}
     *
     * Response:
     * {
     *   "totalCheckins": 12,
     *   "earnedPoints": 1200,
     *   "lastCheckin": {
     *     "branchName": "본점",
     *     "checkinDate": "2025-12-17"
     *   }
     * }
     */
    @GetMapping("/checkin/history/{userNo}")
    public ResponseEntity<Map<String, Object>> getCheckinHistory(
            @PathVariable Long userNo,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 체크인 이력 조회 - userNo: {}", userNo);

            // 인증 확인 (옵션)
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            int userId = userNo.intValue();
            List<BranchCheckinDTO> history = branchCheckinService.getCheckinHistory(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("totalCheckins", history != null ? history.size() : 0);
            response.put("earnedPoints", (history != null ? history.size() : 0) * 100);

            // 마지막 체크인 정보
            if (history != null && !history.isEmpty()) {
                BranchCheckinDTO lastCheckin = history.get(0);
                Map<String, Object> lastCheckinInfo = new HashMap<>();
                lastCheckinInfo.put("branchName", lastCheckin.getBranchName());
                lastCheckinInfo.put("checkinDate", lastCheckin.getCheckinDate());
                response.put("lastCheckin", lastCheckinInfo);
            }

            log.info("✅ 체크인 이력 조회 완료 - 총 {}회", history != null ? history.size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 체크인 이력 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "체크인 이력 조회 실패"));
        }
    }

    /**
     * 영업점 체크인 수행
     * POST /api/flutter/checkin
     *
     * Request Body:
     * {
     *   "userId": 231837269,
     *   "branchId": 1,
     *   "latitude": 35.1234,
     *   "longitude": 129.1234
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "branchName": "본점",
     *   "earnedPoints": 100,
     *   "message": "체크인에 성공했습니다! 100 포인트가 지급되었습니다."
     * }
     */
    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkin(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 체크인 요청 - request: {}", request);

            // 인증 확인 (옵션)
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            Integer userId = (Integer) request.get("userId");
            Integer branchId = (Integer) request.get("branchId");
            Double latitude = ((Number) request.get("latitude")).doubleValue();
            Double longitude = ((Number) request.get("longitude")).doubleValue();

            if (userId == null || branchId == null || latitude == null || longitude == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "필수 파라미터가 누락되었습니다."));
            }

            String result = branchCheckinService.processCheckin(userId, branchId, latitude, longitude);

            Map<String, Object> response = new HashMap<>();
            if ("SUCCESS".equals(result)) {
                // 지점 정보 조회
                BranchDTO branch = branchMapper.selectBranchById(branchId);

                response.put("success", true);
                response.put("branchName", branch != null ? branch.getBranchName() : "");
                response.put("earnedPoints", 100);  // 2025-12-17 - 실제 지급 포인트와 일치하도록 수정 - 작성자: 진원
                response.put("message", "체크인에 성공했습니다! 100 포인트가 지급되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", result);
            }

            log.info("✅ 체크인 완료 - userId: {}, branchId: {}, result: {}", userId, branchId, result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 체크인 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "체크인 처리 중 오류가 발생했습니다."));
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 10. 포인트 이력 API (인증 필요)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 포인트 이력 조회
     * GET /api/flutter/points/history/{userNo}
     *
     * Response:
     * [
     *   {
     *     "pointId": 1,
     *     "pointAmount": 100,
     *     "pointType": "EARN",
     *     "description": "회원가입 보너스",
     *     "createdAt": "2025-12-10T10:00:00"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/points/history/{userNo}")
    public ResponseEntity<?> getPointHistory(
            @PathVariable Long userNo,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 포인트 이력 조회 - userNo: {}", userNo);

            // 인증 확인 (옵션)
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            int userId = userNo.intValue();

            // 포인트 이력 조회 (기본 페이지: 1, 사이즈: 100)
            Map<String, Object> historyData = pointService.getPointHistory(userId, 1, 100);

            List<?> historyList = (List<?>) historyData.get("historyList");

            log.info("✅ 포인트 이력 조회 완료 - {}건", historyList != null ? historyList.size() : 0);
            return ResponseEntity.ok(historyList != null ? historyList : List.of());
        } catch (Exception e) {
            log.error("❌ 포인트 이력 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * 🔥 사용자 프로필 조회 (Flutter 전용)
     * 작성일: 2025-12-18
     * 작성자: 진원
     *
     * @param userNo 사용자 번호
     * @return 프로필 정보 (기본정보 + 포인트 + 가입상품 수)
     */
    @GetMapping("/profile/{userNo}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userNo) {
        try {
            log.info("📱 [Flutter] 프로필 조회 요청 - userNo: {}", userNo);

            // 1. 사용자 기본 정보 조회
            UsersDTO user = memberMapper.findByUserNo(userNo);
            if (user == null) {
                log.warn("⚠️ 사용자를 찾을 수 없음 - userNo: {}", userNo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            // 2. 포인트 정보 조회
            int userId = Integer.parseInt(user.getUserId());
            UserPointDTO pointInfo = pointMapper.selectUserPointByUserId(userId);
            int totalPoints = (pointInfo != null) ? pointInfo.getTotalEarned() : 0;
            int availablePoints = (pointInfo != null) ? pointInfo.getCurrentPoint() : 0;
            int usedPoints = (pointInfo != null) ? pointInfo.getTotalUsed() : 0;

            // 3. 가입 상품 수 조회
            int countUserItems = myMapper.countUserItems(user.getUserId());

            // 4. 최근 접속 시간 (현재 시간으로 설정)
            String connectTime = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 5. 응답 데이터 구성
            Map<String, Object> profile = new HashMap<>();
            profile.put("userNo", user.getUserNo());
            profile.put("userId", user.getUserId());

            // AES 복호화 (암호화된 필드)
            try {
                profile.put("userName", user.getUserName() != null ? AESUtil.decrypt(user.getUserName()) : null);
                profile.put("email", user.getEmail() != null ? AESUtil.decrypt(user.getEmail()) : null);
                profile.put("hp", user.getHp() != null ? AESUtil.decrypt(user.getHp()) : null);
            } catch (Exception e) {
                log.warn("⚠️ AES 복호화 실패, 원본 데이터 사용", e);
                profile.put("userName", user.getUserName());
                profile.put("email", user.getEmail());
                profile.put("hp", user.getHp());
            }

            profile.put("zip", user.getZip());
            profile.put("addr1", user.getAddr1());
            profile.put("addr2", user.getAddr2());
            profile.put("lastConnectTime", connectTime);
            profile.put("connectTime", connectTime); // 호환성을 위해 두 가지 모두 제공

            // 포인트 정보
            profile.put("totalPoints", totalPoints);
            profile.put("availablePoints", availablePoints);
            profile.put("usedPoints", usedPoints);
            profile.put("remainPoints", usedPoints); // 호환성을 위해

            // 가입 상품 수
            profile.put("countUserItems", countUserItems);

            log.info("✅ 프로필 조회 완료 - userId: {}, 포인트: {}, 가입상품: {}개",
                    user.getUserId(), availablePoints, countUserItems);

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            log.error("❌ 프로필 조회 실패 - userNo: {}", userNo, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "프로필 조회 중 오류가 발생했습니다"));
        }
    }


    /**
     * ✅ 카테고리 목록 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("카테고리 목록 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "조회 실패"));
        }
    }

    /**
     * ✅ 카테고리별 상품 조회
     */
    @GetMapping("/products/by-category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable int categoryId) {
        try {
            List<ProductDTO> products = productService.getProductsByCategory(categoryId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 실패: categoryId={}", categoryId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "조회 실패"));
        }
    }

    /**
     * ✅ 뉴스 URL 분석
     */
    @PostMapping("/news/analyze/url")
    public ResponseEntity<?> analyzeNewsUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL이 비어있습니다"));
        }

        try {
            log.info("뉴스 URL 분석 시작: {}", url);
            NewsAnalysisResult result = newsCrawlerService.analyzeUrlWithAI(url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("뉴스 분석 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "분석 실패: " + e.getMessage()));
        }
    }

    /**
     * ✅ 이미지 분석 (선택사항)
     */
    @PostMapping("/news/analyze/image")
    public ResponseEntity<?> analyzeNewsImage(@RequestParam("file") MultipartFile file) {

        // ✅ 상세 로깅
        System.out.println("========================================");
        System.out.println("📸 이미지 분석 요청 받음");
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("크기: " + file.getSize() + " bytes");
        System.out.println("Content-Type: " + file.getContentType());
        System.out.println("isEmpty: " + file.isEmpty());
        System.out.println("========================================");

        if (file.isEmpty()) {
            System.err.println("❌ 파일이 비어있습니다!");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일이 비어있습니다"));
        }

        try {
            System.out.println("✅ OCR 시작...");
            NewsAnalysisResult result = newsCrawlerService.analyzeImage(file);

            System.out.println("✅ 분석 완료!");
            System.out.println("제목: " + result.getTitle());
            System.out.println("요약 길이: " + (result.getSummary() != null ? result.getSummary().length() : 0));
            System.out.println("========================================");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ 입력 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "입력 오류",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            System.err.println("❌ 이미지 분석 실패!");
            System.err.println("에러 타입: " + e.getClass().getName());
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "이미지 분석 실패",
                            "message", e.getMessage() != null ? e.getMessage() : "알 수 없는 오류",
                            "type", e.getClass().getSimpleName()
                    ));
        }
    }

    /**
     * 만보기 포인트 지급
     */
    @PostMapping("/points/steps/earn")
    public ResponseEntity<?> earnStepsPoints(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long userNo = ((Number) request.get("userNo")).longValue();
            int steps = ((Number) request.get("steps")).intValue();
            String date = (String) request.get("date"); // "2024-12-19" 형식

            log.info("📱 [Flutter] 만보기 포인트 지급 요청 - userNo: {}, steps: {}", userNo, steps);

            // 목표 미달성 체크
            if (steps < 10000) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "10,000보를 달성해야 포인트를 받을 수 있습니다"));
            }

            // 포인트 계산 (10,000보 = 100포인트)
            int pointsToEarn = 100;

            // 포인트 지급
            boolean success = pointService.earnPoints(
                    userNo.intValue(),
                    pointsToEarn,
                    String.format("만보기 목표 달성 (%d보)", steps)
            );

            if (success) {
                log.info("✅ 만보기 포인트 지급 완료: {}P", pointsToEarn);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "earnedPoints", pointsToEarn,
                        "message", pointsToEarn + "포인트가 지급되었습니다!"
                ));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "포인트 지급 실패"));
            }

        } catch (Exception e) {
            log.error("❌ 만보기 포인트 지급 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류"));
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ESG 바다청소 낚시 게임 API
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 낚시 결과 제출 및 포인트 적립
     * POST /api/flutter/fishing/submit
     */
    @PostMapping("/fishing/submit")
    public ResponseEntity<?> submitFishingResult(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = request.get("userId").toString();
            String trashType = (String) request.get("trashType");
            int points = ((Number) request.get("points")).intValue();
            String catchTime = (String) request.get("catchTime");

            log.info("📱 [Flutter] 낚시 결과 제출 - userId: {}, trashType: {}, points: {}",
                    userId, trashType, points);

            // 포인트 지급
            boolean success = pointService.earnPoints(
                    Integer.parseInt(userId),
                    points,
                    String.format("ESG 바다청소 낚시 (%s 수거)", trashType)
            );

            if (success) {
                log.info("✅ 낚시 포인트 지급 완료: {}P", points);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "earnedPoints", points,
                        "message", points + "포인트가 지급되었습니다!",
                        "trashType", trashType
                ));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("success", false, "message", "포인트 지급 실패"));
            }

        } catch (Exception e) {
            log.error("❌ 낚시 포인트 지급 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 랜덤 쓰레기 조회
     * GET /api/flutter/fishing/random-trash
     */
    @GetMapping("/fishing/random-trash")
    public ResponseEntity<?> getRandomTrash() {
        try {
            // 쓰레기 타입과 포인트 정의
            String[][] trashData = {
                    {"plastic", "플라스틱 병", "10", "🍾"},
                    {"can", "캔", "15", "🥫"},
                    {"bag", "비닐봉지", "20", "🛍️"},
                    {"bottle", "유리병", "25", "🍶"},
                    {"tire", "폐타이어", "50", "🛞"},
                    {"net", "어망", "100", "🌐"}
            };

            Random random = new Random();
            String[] selectedTrash = trashData[random.nextInt(trashData.length)];

            Map<String, Object> trash = Map.of(
                    "type", selectedTrash[0],
                    "name", selectedTrash[1],
                    "points", Integer.parseInt(selectedTrash[2]),
                    "emoji", selectedTrash[3]
            );

            log.info("📱 [Flutter] 랜덤 쓰레기 조회: {}", selectedTrash[1]);
            return ResponseEntity.ok(trash);

        } catch (Exception e) {
            log.error("❌ 랜덤 쓰레기 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류"));
        }
    }

    /**
     * 오늘의 낚시 통계 조회
     * GET /api/flutter/fishing/stats/{userId}
     */
    @GetMapping("/fishing/stats/{userId}")
    public ResponseEntity<?> getTodayFishingStats(@PathVariable String userId) {
        try {
            log.info("📱 [Flutter] 낚시 통계 조회 - userId: {}", userId);

            // TODO: DB에서 오늘의 낚시 통계 조회
            // 현재는 임시 데이터 반환
            Map<String, Object> stats = Map.of(
                    "todayCatches", 0,
                    "todayPoints", 0,
                    "totalCatches", 0,
                    "totalPoints", 0
            );

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ 낚시 통계 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류"));
        }
    }

    private int calculateStepsPoints(int steps) {
        // 10,000보 달성 시 100포인트
        if (steps >= 10000) return 100;
        // 5,000보 달성 시 50포인트
        if (steps >= 5000) return 50;
        // 그 외
        return 0;
    }

    private boolean checkIfAlreadyEarned(int userId, String date) {
        // TODO: DB에서 오늘 날짜로 만보기 포인트 지급 이력이 있는지 체크
        // PointMapper에 메서드 추가 필요
        return false;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 사용자 프로필 관리 API (닉네임, 아바타)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 닉네임 중복 확인
     * GET /api/flutter/profile/check-nickname?nickname=xxx
     */
    @GetMapping("/profile/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        try {
            log.info("📱 [Flutter] 닉네임 중복 확인 - nickname: {}", nickname);

            // 닉네임 유효성 검사
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("available", false, "message", "닉네임을 입력해주세요."));
            }

            if (nickname.length() < 2 || nickname.length() > 20) {
                return ResponseEntity.badRequest()
                        .body(Map.of("available", false, "message", "닉네임은 2-20자 이내로 입력해주세요."));
            }

            // 중복 확인
            int count = memberMapper.countByNickname(nickname.trim());
            boolean available = (count == 0);

            log.info("✅ 닉네임 중복 확인 완료 - available: {}", available);

            return ResponseEntity.ok(Map.of(
                    "available", available,
                    "message", available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
            ));

        } catch (Exception e) {
            log.error("❌ 닉네임 중복 확인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("available", false, "message", "서버 오류"));
        }
    }

    /**
     * 닉네임 업데이트
     * POST /api/flutter/profile/update-nickname
     */
    @PostMapping("/profile/update-nickname")
    public ResponseEntity<?> updateNickname(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long userNo = ((Number) request.get("userNo")).longValue();
            String nickname = (String) request.get("nickname");

            log.info("📱 [Flutter] 닉네임 업데이트 - userNo: {}, nickname: {}", userNo, nickname);

            // 닉네임 유효성 검사
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "닉네임을 입력해주세요."));
            }

            if (nickname.length() < 2 || nickname.length() > 20) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "닉네임은 2-20자 이내로 입력해주세요."));
            }

            // 중복 확인
            int count = memberMapper.countByNickname(nickname.trim());
            if (count > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미 사용 중인 닉네임입니다."));
            }

            // 닉네임 업데이트
            int result = memberMapper.updateNickname(userNo, nickname.trim());

            if (result > 0) {
                log.info("✅ 닉네임 업데이트 완료");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "닉네임이 변경되었습니다.",
                        "nickname", nickname.trim()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "닉네임 변경 실패"));
            }

        } catch (Exception e) {
            log.error("❌ 닉네임 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 아바타 이미지 업로드
     * POST /api/flutter/profile/upload-avatar
     */
    @PostMapping("/profile/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("userNo") Long userNo,
            @RequestParam("avatar") MultipartFile avatar,
            Authentication authentication) {
        try {
            log.info("📱 [Flutter] 아바타 업로드 - userNo: {}, fileName: {}", userNo, avatar.getOriginalFilename());

            // 파일 검증
            if (avatar.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일을 선택해주세요."));
            }

            // 파일 크기 제한 (5MB)
            if (avatar.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "파일 크기는 5MB 이하여야 합니다."));
            }

            // 파일 형식 검증 (이미지만 허용)
            String contentType = avatar.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일만 업로드 가능합니다."));
            }

            // 파일 저장 경로 설정
            String uploadDir = "C:/upload/avatars/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // 파일명 생성 (중복 방지)
            String originalFilename = avatar.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = "avatar_" + userNo + "_" + System.currentTimeMillis() + extension;
            String savedPath = uploadDir + savedFilename;

            // 파일 저장
            avatar.transferTo(new java.io.File(savedPath));

            // DB 업데이트
            String dbPath = "/upload/avatars/" + savedFilename;
            int result = memberMapper.updateAvatarImage(userNo, dbPath);

            if (result > 0) {
                log.info("✅ 아바타 업로드 완료: {}", dbPath);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "아바타가 변경되었습니다.",
                        "avatarUrl", dbPath
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "아바타 변경 실패"));
            }

        } catch (Exception e) {
            log.error("❌ 아바타 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류: " + e.getMessage()));
        }
    }

}