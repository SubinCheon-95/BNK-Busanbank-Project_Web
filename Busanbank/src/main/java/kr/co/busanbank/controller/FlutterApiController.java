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
     *
     * POST /api/flutter/join/auth
     *
     * TODO: 로그인 구현 후 작성
     * - SecurityContext에서 userId 추출
     * - 나머지는 게스트와 동일
     */
    @PostMapping("/join/auth")
    public ResponseEntity<?> joinAsAuth(
            @RequestBody ProductJoinRequestDTO joinRequest,
            Authentication authentication
    ) {
        try {
            log.info("📱 [Flutter-AUTH] 인증 가입 요청 수신");
            log.info("   productNo: {}", joinRequest.getProductNo());
            log.info("   usedPoints: {}", joinRequest.getUsedPoints());  // ✅ 확인!
            log.info("   selectedCouponId: {}", joinRequest.getSelectedCouponId());

            // 1. JWT에서 userId 추출
            String userId = authentication.getName();

            // 2. userId로 userNo 조회
            Long userNo = memberMapper.findUserNoByUserId(userId);
            joinRequest.setUserId(userNo.intValue());

            // 3. 실제 가입 처리
            boolean result = productJoinService.processJoin(joinRequest);

            if (!result) {
                log.error("❌ [Flutter-AUTH] 상품 가입 처리 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("상품 가입 처리 중 오류가 발생했습니다.");
            }

            log.info("🎉 [Flutter-AUTH] 상품 가입 완료!");
            log.info("   userId: {}, userNo: {}", userId, userNo);
            log.info("   productNo: {}", joinRequest.getProductNo());
            log.info("   usedPoints: {}", joinRequest.getUsedPoints());

            return ResponseEntity.ok("상품 가입이 완료되었습니다.");

        } catch (Exception e) {
            log.error("❌ [Flutter-AUTH] 가입 처리 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 8. 출석체크 API (2025-12-16 작성자: 진원)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 출석 현황 조회
     * GET /api/flutter/attendance/status/{userId}
     *
     * Response:
     * {
     *   "isCheckedToday": false,
     *   "consecutiveDays": 3,
     *   "totalDays": 15,
     *   "totalPoints": 150,
     *   "weeklyAttendance": [true, true, false, false, false, false, false]
     * }
     */
    @GetMapping("/attendance/status/{userId}")
    public ResponseEntity<?> getAttendanceStatus(@PathVariable Integer userId) {
        try {
            log.info("📱 [Flutter] 출석 현황 조회 - userId: {}", userId);

            // 오늘 출석 여부
            int todayCount = attendanceMapper.countTodayAttendance(userId);
            boolean isCheckedToday = todayCount > 0;

            // 최근 출석 정보 (연속 출석 일수)
            AttendanceDTO latest = attendanceMapper.selectLatestAttendance(userId);
            int consecutiveDays = latest != null ? latest.getConsecutiveDays() : 0;

            // 총 출석일수
            int totalDays = attendanceMapper.countTotalAttendance(userId);

            // 총 획득 포인트
            Integer totalPoints = attendanceMapper.selectTotalEarnedPoints(userId);
            if (totalPoints == null) totalPoints = 0;

            // 이번 주 출석 현황 (월~일)
            List<AttendanceDTO> weeklyList = attendanceMapper.selectWeeklyAttendance(userId);
            boolean[] weeklyAttendance = new boolean[7];
            for (AttendanceDTO att : weeklyList) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(att.getAttendanceDate());
                int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1=일요일, 2=월요일
                int index = (dayOfWeek == 1) ? 6 : dayOfWeek - 2; // 월요일=0, 일요일=6
                if (index >= 0 && index < 7) {
                    weeklyAttendance[index] = true;
                }
            }

            var response = new java.util.HashMap<String, Object>();
            response.put("isCheckedToday", isCheckedToday);
            response.put("consecutiveDays", consecutiveDays);
            response.put("totalDays", totalDays);
            response.put("totalPoints", totalPoints);
            response.put("weeklyAttendance", weeklyAttendance);

            log.info("✅ 출석 현황 조회 완료 - 연속: {}일, 총: {}일", consecutiveDays, totalDays);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 출석 현황 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 출석 체크 등록
     * POST /api/flutter/attendance/check
     *
     * Request Body:
     * {
     *   "userId": 1
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "출석 체크 완료!",
     *   "earnedPoints": 10,
     *   "consecutiveDays": 4,
     *   "bonusPoints": 0
     * }
     */
    @PostMapping("/attendance/check")
    public ResponseEntity<?> checkAttendance(@RequestBody java.util.Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            log.info("📱 [Flutter] 출석 체크 요청 - userId: {}", userId);

            // 오늘 이미 출석했는지 확인
            int todayCount = attendanceMapper.countTodayAttendance(userId);
            if (todayCount > 0) {
                log.warn("⚠️ 이미 출석 완료 - userId: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                                "success", false,
                                "message", "오늘은 이미 출석체크를 완료했습니다"
                        ));
            }

            // 연속 출석 일수 계산
            AttendanceDTO latest = attendanceMapper.selectLatestAttendance(userId);
            int consecutiveDays = 1;

            if (latest != null) {
                java.util.Calendar lastDate = java.util.Calendar.getInstance();
                lastDate.setTime(latest.getAttendanceDate());
                lastDate.add(java.util.Calendar.DATE, 1); // 어제 날짜 + 1

                java.util.Calendar today = java.util.Calendar.getInstance();

                // 어제 출석했으면 연속, 아니면 1로 초기화
                if (lastDate.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        lastDate.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
                    consecutiveDays = latest.getConsecutiveDays() + 1;
                }
            }

            // 기본 포인트 10점
            int earnedPoints = 10;

            // 7일 연속 출석 보너스 50점
            int bonusPoints = 0;
            if (consecutiveDays % 7 == 0) {
                bonusPoints = 50;
                earnedPoints += bonusPoints;
            }

            // 출석 등록
            AttendanceDTO attendance = AttendanceDTO.builder()
                    .userId(userId)
                    .consecutiveDays(consecutiveDays)
                    .earnedPoints(earnedPoints)
                    .build();

            int result = attendanceMapper.insertAttendance(attendance);

            if (result > 0) {
                // 포인트 적립 (POINTHISTORY 테이블에 추가)
                PointHistoryDTO pointHistory = PointHistoryDTO.builder()
                        .userId(userId)
                        .pointChange(earnedPoints)
                        .changeType("EARN")
                        .changeReason("출석체크")
                        .build();
                pointMapper.insertPointHistory(pointHistory);

                // UserPoint 업데이트
                pointMapper.updateUserPointAfterEarn(userId, earnedPoints);

                var response = new java.util.HashMap<String, Object>();
                response.put("success", true);
                response.put("message", "출석 체크 완료!");
                response.put("earnedPoints", earnedPoints);
                response.put("consecutiveDays", consecutiveDays);
                response.put("bonusPoints", bonusPoints);

                log.info("🎉 출석 체크 완료 - userId: {}, 연속: {}일, 포인트: {}P",
                        userId, consecutiveDays, earnedPoints);
                return ResponseEntity.ok(response);
            } else {
                throw new Exception("출석 등록 실패");
            }

        } catch (Exception e) {
            log.error("❌ 출석 체크 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "출석 체크 중 오류가 발생했습니다: " + e.getMessage()
                    ));
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 9. 영업점 체크인 API (2025-12-16 작성자: 진원)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * 체크인 기록 조회
     * GET /api/flutter/checkin/history/{userId}
     *
     * Response:
     * {
     *   "totalCheckins": 5,
     *   "earnedPoints": 100,
     *   "lastCheckin": {
     *     "branchName": "서면지점",
     *     "checkinDate": "2025-12-15"
     *   },
     *   "recentCheckins": [...]
     * }
     */
    @GetMapping("/checkin/history/{userId}")
    public ResponseEntity<?> getCheckinHistory(@PathVariable Integer userId) {
        try {
            log.info("📱 [Flutter] 체크인 기록 조회 - userId: {}", userId);

            // 전체 체크인 목록
            List<BranchCheckinDTO> allCheckins = branchCheckinMapper.selectCheckinsByUserId(userId);

            // 총 체크인 횟수
            int totalCheckins = allCheckins.size();

            // 총 획득 포인트
            int earnedPoints = allCheckins.stream()
                    .mapToInt(c -> c.getPointsReceived() != null ? c.getPointsReceived() : 0)
                    .sum();

            // 최근 체크인 정보
            java.util.Map<String, Object> lastCheckin = null;
            if (!allCheckins.isEmpty()) {
                BranchCheckinDTO last = allCheckins.get(0);
                lastCheckin = new java.util.HashMap<>();
                lastCheckin.put("branchName", last.getBranchName());
                lastCheckin.put("checkinDate", last.getCheckinDate());
            }

            // 최근 10개만
            List<BranchCheckinDTO> recentCheckins = allCheckins.stream()
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());

            var response = new java.util.HashMap<String, Object>();
            response.put("totalCheckins", totalCheckins);
            response.put("earnedPoints", earnedPoints);
            response.put("lastCheckin", lastCheckin);
            response.put("recentCheckins", recentCheckins);

            log.info("✅ 체크인 기록 조회 완료 - 총: {}회, 포인트: {}P", totalCheckins, earnedPoints);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 체크인 기록 조회 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 영업점 체크인 등록
     * POST /api/flutter/checkin
     *
     * Request Body:
     * {
     *   "userId": 1,
     *   "branchId": 1,
     *   "latitude": 35.1234,
     *   "longitude": 129.1234
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "체크인 완료!",
     *   "branchName": "서면지점",
     *   "earnedPoints": 20
     * }
     */
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestBody java.util.Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Integer branchId = (Integer) request.get("branchId");
            Double latitude = request.get("latitude") != null ?
                    ((Number) request.get("latitude")).doubleValue() : null;
            Double longitude = request.get("longitude") != null ?
                    ((Number) request.get("longitude")).doubleValue() : null;

            log.info("📱 [Flutter] 체크인 요청 - userId: {}, branchId: {}", userId, branchId);

            // 오늘 이미 체크인했는지 확인
            int todayCount = branchCheckinMapper.countTodayCheckin(userId);
            if (todayCount > 0) {
                log.warn("⚠️ 오늘 이미 체크인 완료 - userId: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                                "success", false,
                                "message", "오늘은 이미 체크인을 완료했습니다"
                        ));
            }

            // 체크인 포인트 (기본 20점)
            int points = 20;

            // 체크인 등록
            BranchCheckinDTO checkin = BranchCheckinDTO.builder()
                    .userId(userId)
                    .branchId(branchId)
                    .latitude(latitude)
                    .longitude(longitude)
                    .pointsReceived(points)
                    .build();

            int result = branchCheckinMapper.insertCheckin(checkin);

            if (result > 0) {
                // 포인트 적립 (POINTHISTORY 테이블에 추가)
                PointHistoryDTO pointHistory = PointHistoryDTO.builder()
                        .userId(userId)
                        .pointChange(points)
                        .changeType("EARN")
                        .changeReason("영업점 체크인")
                        .build();
                pointMapper.insertPointHistory(pointHistory);

                // UserPoint 업데이트
                pointMapper.updateUserPointAfterEarn(userId, points);

                // 지점 정보 조회
                BranchDTO branch = branchMapper.selectAllBranches().stream()
                        .filter(b -> b.getBranchId().equals(branchId))
                        .findFirst()
                        .orElse(null);

                var response = new java.util.HashMap<String, Object>();
                response.put("success", true);
                response.put("message", "체크인 완료!");
                response.put("branchName", branch != null ? branch.getBranchName() : "");
                response.put("earnedPoints", points);

                log.info("🎉 체크인 완료 - userId: {}, branchId: {}, 포인트: {}P",
                        userId, branchId, points);
                return ResponseEntity.ok(response);
            } else {
                throw new Exception("체크인 등록 실패");
            }

        } catch (Exception e) {
            log.error("❌ 체크인 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "체크인 중 오류가 발생했습니다: " + e.getMessage()
                    ));
        }
    }
}