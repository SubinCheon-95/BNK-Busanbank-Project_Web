package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.co.busanbank.dto.*;
import kr.co.busanbank.dto.quiz.UserStatusDTO;
import kr.co.busanbank.entity.quiz.UserLevel;
import kr.co.busanbank.mapper.UserCouponMapper;
import kr.co.busanbank.repository.quiz.UserLevelRepository;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 날짜 : 2025/11/21
 * 이름 : 김수진
 * 내용 : ProductJoinController
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/prod/productjoin")
@SessionAttributes("joinRequest")
public class ProductJoinController {

    private final ProductService productService;
    private final ProductTermsService productTermsService;
    private final ProductJoinService productJoinService;
    private final BranchService branchService;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;
    // ✅ UserLevelRepository 게임 포인트 100점당 글미 0.1추가
    private final UserLevelRepository userLevelRepository;
    private final UserCouponMapper userCouponMapper;
    // 작성자: 진원, 2025-11-29, 통합 포인트 시스템 사용을 위해 PointService 추가
    private final PointService pointService;

    /**
     * Session에 저장할 joinRequest 객체 초기화
     */
    @ModelAttribute("joinRequest")
    public ProductJoinRequestDTO joinRequest() {
        return new ProductJoinRequestDTO();
    }

    // ========================================
    // STEP 1: 필수 확인 사항
    // ========================================

    /**
     * STEP 1: 필수 확인 사항 페이지
     */
    @GetMapping("/step1")
    public String step1(@RequestParam("productNo") int productNo, Model model) {
        log.info("STEP 1 진입 - productNo: {}", productNo);

        ProductDTO product = productService.getProductById(productNo);
        ProductDetailDTO detail = productService.getProductDetail(productNo);
        List<ProductTermsDTO> terms = productTermsService.getTermsByProductNo(productNo);

        model.addAttribute("product", product);
        model.addAttribute("detail", detail);
        model.addAttribute("terms", terms);

        return "product/productJoinStage/registerstep01";
    }

    /**
     * STEP 1 처리 → STEP 2로 이동
     */
    @PostMapping("/step1")
    public String processStep1(
            @RequestParam("productNo") int productNo,
            @RequestParam(value = "agreedTermIds", required = false) List<Integer> agreedTermIds,
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            Model model) {

        log.info("STEP 1 처리 - productNo: {}, agreedTermIds: {}", productNo, agreedTermIds);

        if (!productTermsService.validateRequiredTerms(productNo, agreedTermIds)) {
            model.addAttribute("error", "모든 필수 약관에 동의해주세요.");
            return step1(productNo, model);
        }

        joinRequest.setProductNo(productNo);
        joinRequest.setAgreedTermIds(agreedTermIds);

        return "redirect:/prod/productjoin/step2";
    }

    // ========================================
    // STEP 2: 정보 입력
    // ========================================

    @GetMapping("/step2")
    public String step2(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 2 진입 - productNo: {}, userNo: {}",
                joinRequest.getProductNo(),
                user != null ? user.getUserNo() : "null");

        if (joinRequest.getProductNo() == null) {
            log.warn("productNo가 없습니다. 상품 목록으로 이동합니다.");
            return "redirect:/prod/list/main";
        }

        if (user == null || user.getUserNo() == 0) {
            log.warn("⚠️ 로그인 필요 - 로그인 페이지로 이동");
            model.addAttribute("needLogin", true);
            model.addAttribute("redirectUrl", "/prod/productjoin/step2");
            return "product/productJoinStage/registerstep02";
        }

        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        ProductDetailDTO detail = productService.getProductDetail(joinRequest.getProductNo());
        List<BranchDTO> branches = branchService.getAllBranches();

        model.addAttribute("product", product);
        model.addAttribute("detail", detail);
        model.addAttribute("branches", branches);
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("userHp", user.getHp());
        model.addAttribute("userEmail", user.getEmail());

        log.info("✅ 고객 정보 연계 완료: 이름={}, 휴대폰={}, 이메일={}",
                user.getUserName(), user.getHp(), user.getEmail());

        return "product/productJoinStage/registerstep02";
    }

    @PostMapping("/step2")
    public String processStep2(
            @Validated(ProductJoinRequestDTO.Step2.class) @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            BindingResult result,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 2 처리 - principalAmount: {}, contractTerm: {}, branchId: {}, empId: {}",
                joinRequest.getPrincipalAmount(),
                joinRequest.getContractTerm(),
                joinRequest.getBranchId(),
                joinRequest.getEmpId());

        // 🔥 추가 로그: 입력 값 RAW 체크
        log.info("🔥 입력 PW RAW: '{}'", joinRequest.getAccountPassword());
        log.info("🔥 입력 PW 확인 RAW: '{}'", joinRequest.getAccountPasswordConfirm());
        log.info("🔥 DB PW RAW: '{}'", user.getAccountPassword());

        if (result.hasErrors()) {
            log.error("입력 검증 실패: {}", result.getAllErrors());
            model.addAttribute("error", "입력 정보를 확인해주세요.");
            return step2(joinRequest, user, model);
        }

        if (joinRequest.getAccountPassword() == null ||
                joinRequest.getAccountPasswordConfirm() == null ||
                !joinRequest.getAccountPassword().equals(joinRequest.getAccountPasswordConfirm())) {
            log.warn("계좌 비밀번호 확인 불일치");
            model.addAttribute("error", "계좌 비밀번호가 일치하지 않습니다.");
            return step2(joinRequest, user, model);
        }


        // ✅ 원본 비밀번호를 Session에 저장 (평문)
        String originalPassword = joinRequest.getAccountPassword();
        joinRequest.setAccountPasswordOriginal(originalPassword);

        log.info("📌 원본 비밀번호 Session에 저장 완료 (평문)");

        // 2. ✅ 계좌 비밀번호 DB 비교
        try {
            String inputPassword = joinRequest.getAccountPassword(); // 사용자 입력 (평문)
            String dbPassword = user.getAccountPassword();           // DB 저장값 (암호화됨)

            log.info("🔍 비밀번호 비교 시작");
            log.info("🔍 비밀번호 비교 시작");
            log.info("   입력값 LENGTH: {}", inputPassword != null ? inputPassword.length() : null);
            log.info("   입력값 ASCII: {}", inputPassword != null ? inputPassword.chars().toArray() : "null");

            log.info("   DB값 LENGTH: {}", dbPassword != null ? dbPassword.length() : "null");

            boolean passwordMatches = false;

            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ DB에 계좌 비밀번호가 없음");
                model.addAttribute("error", "계좌 비밀번호가 설정되지 않았습니다.");
                return step2(joinRequest, user, model);

            } else if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$")) {
                // BCrypt 방식
                log.info("📌 BCrypt 방식으로 비교");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);

            } else {
                // AES 또는 평문
                try {
                    String decryptedPassword = AESUtil.decrypt(dbPassword);
                    log.info("📌 AES 복호화 성공");
                    passwordMatches = inputPassword.equals(decryptedPassword);
                } catch (Exception e) {
                    log.info("📌 평문으로 비교");
                    passwordMatches = inputPassword.equals(dbPassword);
                }
            }

            if (!passwordMatches) {
                log.warn("❌ 계좌 비밀번호 불일치");

                // Session 초기화
                int productNo = joinRequest.getProductNo();
                joinRequest.setProductNo(null);
                joinRequest.setPrincipalAmount(null);
                joinRequest.setContractTerm(null);
                joinRequest.setAccountPassword(null);
                joinRequest.setAccountPasswordOriginal(null); // ✅ 원본도 초기화

                // Session초기화
//                int productNo = joinRequest.getProductNo();
//                joinRequest.setProductNo(null);
//                joinRequest.setPrincipalAmount(null);
//                joinRequest.setContractTerm(null);
//                joinRequest.setAccountPassword(null);
//                joinRequest.setAccountPasswordConfirm(null);
//                joinRequest.setBranchId(null);
//                joinRequest.setEmpId(null);
//                joinRequest.setNotificationSms(null);
//                joinRequest.setNotificationEmail(null);
//                joinRequest.setSmsVerified(false);
//                joinRequest.setEmailVerified(false);


                return "redirect:/prod/view?productNo=" + productNo + "&error=password";
            }

            log.info("✅ 계좌 비밀번호 일치");

        } catch (Exception e) {
            log.error("계좌 비밀번호 검증 중 오류", e);

            int productNo = joinRequest.getProductNo();
            joinRequest.setProductNo(null);
            joinRequest.setPrincipalAmount(null);
            joinRequest.setContractTerm(null);

            return "redirect:/prod/view?productNo=" + productNo + "&error=system";
        }


        // 알림 설정 검증 (기존 코드 유지)
        boolean hasSmsNotification = "Y".equals(joinRequest.getNotificationSms());
        boolean hasEmailNotification = "Y".equals(joinRequest.getNotificationEmail());

        if (!hasSmsNotification && !hasEmailNotification) {
            log.warn("알림 설정 미선택");
            model.addAttribute("error", "만기 알림 설정을 하나 이상 선택해주세요.");
            return step2(joinRequest, user, model);
        }

        if (hasSmsNotification && !Boolean.TRUE.equals(joinRequest.getSmsVerified())) {
            log.warn("SMS 인증 미완료");
            model.addAttribute("error", "SMS 인증을 완료해주세요.");
            return step2(joinRequest, user, model);
        }

        if (hasEmailNotification && !Boolean.TRUE.equals(joinRequest.getEmailVerified())) {
            log.warn("이메일 인증 미완료");
            model.addAttribute("error", "이메일 인증을 완료해주세요.");
            return step2(joinRequest, user, model);
        }

        // 가입일 설정
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        joinRequest.setStartDate(today);

        // 예상 만기일 계산
        String expectedEndDate = productJoinService.calculateExpectedEndDate(
                today, joinRequest.getContractTerm());
        joinRequest.setExpectedEndDate(expectedEndDate);

        log.info("✅ STEP 2 처리 완료 - 가입일: {}, 만기일: {}", today, expectedEndDate);

        return "redirect:/prod/productjoin/step3";
    }

    // ==============================================
// STEP 3: 금리 확인 (✅ 포인트 금리 추가! 쿠폰 금리 추가!)
// ==================================================

    @GetMapping("/step3")
    public String step3(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 3 진입 - productNo: {}", joinRequest.getProductNo());
        log.info("   principalAmount: {}", joinRequest.getPrincipalAmount());
        log.info("   contractTerm: {}", joinRequest.getContractTerm());

        if (joinRequest.getProductNo() == null || joinRequest.getPrincipalAmount() == null) {
            return "redirect:/prod/list/main";
        }

        // 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());

        // ✅ 1. 기본 금리 계산
        BigDecimal baseRate = product.getBaseRate();
        BigDecimal applyRate = productJoinService.calculateApplyRate(joinRequest.getProductNo());

        // ✅ 2. 포인트 조회 및 포인트 금리 계산
        // 작성자: 진원, 2025-11-29, 기존 UserLevel(JPA) → USERPOINT(MyBatis 통합 시스템)로 변경
        int userPoints = 0;
        BigDecimal pointBonusRate = BigDecimal.ZERO;

        try {
            // 작성자: 진원, 2025-11-29, 통합 포인트 시스템(USERPOINT 테이블)에서 조회
            UserPointDTO userPoint = pointService.getUserPoint(user.getUserNo());

            if (userPoint != null) {
                // 작성자: 진원, 2025-11-29, CURRENTPOINT(사용 가능 포인트)를 사용
                userPoints = userPoint.getCurrentPoint() != null ? userPoint.getCurrentPoint() : 0;

                // 100점당 0.1% 금리 추가
                pointBonusRate = BigDecimal.valueOf(userPoints)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .multiply(BigDecimal.valueOf(0.1))
                        .setScale(2, RoundingMode.HALF_UP);

                log.info("✅ 포인트 금리 계산 완료 (통합 시스템)");
                log.info("   사용자 포인트: {}", userPoints);
                log.info("   포인트 금리: {}%", pointBonusRate);
            } else {
                log.warn("⚠️ 사용자 포인트 정보 없음 - userNo: {}", user.getUserNo());
            }

        } catch (Exception e) {
            log.error("❌ 포인트 조회 실패", e);
        }

        // ========================================
        // ✅ 3. 쿠폰 조회 (새로 추가!)
        // ========================================
        List<UserCouponDTO> availableCoupons = new ArrayList<>();
        Integer categoryId = product.getCategoryId();

        log.info("✅ 쿠폰 조회 시작 - categoryId: {}", categoryId);

        // 카테고리 9번 상품에만 쿠폰 조회
        if (categoryId != null && categoryId == 9) {
            try {
                availableCoupons = userCouponMapper.selectAvailableCouponsByCategory(
                        user.getUserNo(),
                        categoryId
                );
                log.info("✅ 쿠폰 조회 완료: {} 개", availableCoupons.size());

                for (UserCouponDTO coupon : availableCoupons) {
                    log.info("   - {} (+ {}%)", coupon.getCouponName(), coupon.getRateIncrease());
                }
            } catch (Exception e) {
                log.error("❌ 쿠폰 조회 실패", e);
            }
        } else {
            log.info("✅ 카테고리 9번이 아니므로 쿠폰 적용 불가");
        }

        model.addAttribute("availableCoupons", availableCoupons);

        // ✅ 쿠폰 초기화
        if (joinRequest.getSelectedCouponId() == null) {
            joinRequest.setSelectedCouponId(null);
        }
        if (joinRequest.getCouponBonusRate() == null) {
            joinRequest.setCouponBonusRate(0.0);
        }

        // ✅ 4. 최종 금리 = 기본 금리 + 포인트 금리 (쿠폰은 나중에 선택)
        BigDecimal finalApplyRate = applyRate.add(pointBonusRate);

        // ✅ 5. 세션 저장: 사용자가 실제로 선택하기 전에는 usedPoints = 0, pointBonusRate = 0
        joinRequest.setBaseRate(baseRate);
        joinRequest.setApplyRate(finalApplyRate);       // 기본 금리만 세팅
        joinRequest.setPointBonusRate(BigDecimal.ZERO); // 초기 포인트 보너스는 0
        joinRequest.setUserPoints(userPoints);          // 보유 포인트는 보여줌
        joinRequest.setUsedPoints(0);                   // <-- 변경: 초기값 0
        joinRequest.setEarlyTerminateRate(product.getEarlyTerminateRate());

        // ✅ 6. 예상 이자 계산 (최종 금리로 계산)
        BigDecimal expectedInterest = productJoinService.calculateExpectedInterest(
                joinRequest.getPrincipalAmount(),
                finalApplyRate,
                joinRequest.getContractTerm(),
                product.getProductType()
        );
        joinRequest.setExpectedInterest(expectedInterest);

        // ✅ 7. 예상 수령액 계산
        BigDecimal expectedTotal = joinRequest.getPrincipalAmount().add(expectedInterest);
        joinRequest.setExpectedTotal(expectedTotal);

        // ✅ 8. Model에 추가
        model.addAttribute("product", product);
        model.addAttribute("userPoints", userPoints);
        model.addAttribute("pointBonusRate", pointBonusRate);
        model.addAttribute("baseRate", baseRate);  // ✅ 추가!

        log.info("✅ STEP 3 준비 완료");
        log.info("   기본 금리: {}%", baseRate);
        log.info("   포인트 금리: {}%", pointBonusRate);
        log.info("   쿠폰 개수: {} 개", availableCoupons.size());
        log.info("   최종 금리: {}%", finalApplyRate);
        log.info("   예상 이자: {}원", expectedInterest);

        return "product/productJoinStage/registerstep03";
    }

    // ========================================
// ✅ 3. STEP 3 POST 수정 (라인 396-442)
// ========================================

    @PostMapping("/step3")
    public String processStep3(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @RequestParam(value = "usedPoints", required = false, defaultValue = "0") Integer usedPoints,
            @RequestParam(value = "pointBonusRate", required = false, defaultValue = "0.00") BigDecimal pointBonusRate,
            @RequestParam(value = "selectedCouponId", required = false) Integer selectedCouponId,  // ✅ 쿠폰 추가!
            @RequestParam(value = "couponBonusRate", required = false, defaultValue = "0.0") Double couponBonusRate,  // ✅ 쿠폰 금리!
            @RequestParam(value = "applyRate", required = false) BigDecimal applyRate,
            @ModelAttribute("user") UsersDTO user, RedirectAttributes redirectAttributes) {  // ✅ RedirectAttributes 추가!

        log.info("STEP 3 처리");
        log.info("   선택한 포인트: {} P", usedPoints);
        log.info("   포인트 금리: {}%", pointBonusRate);
        log.info("   선택한 쿠폰 ID: {}", selectedCouponId);  // ✅ 쿠폰 로그
        log.info("   쿠폰 금리: {}%", couponBonusRate);      // ✅ 쿠폰 금리 로그
        log.info("   최종 금리: {}%", applyRate);

        // ✅ 선택한 포인트 정보 저장
        joinRequest.setUsedPoints(usedPoints);
        joinRequest.setPointBonusRate(pointBonusRate);

        // ✅ 쿠폰 정보 저장 (새로 추가!)
        joinRequest.setSelectedCouponId(selectedCouponId);
        joinRequest.setCouponBonusRate(couponBonusRate);

        // ✅ 최종 금리 계산 (항상 재계산!)
        BigDecimal calculatedApplyRate = joinRequest.getBaseRate()
                .add(pointBonusRate)
                .add(BigDecimal.valueOf(couponBonusRate));

        joinRequest.setApplyRate(calculatedApplyRate);

        log.info("✅ 최종 금리 계산 완료");
        log.info("   기본금리: {}%", joinRequest.getBaseRate());
        log.info("   포인트금리: {}%", pointBonusRate);
        log.info("   쿠폰금리: {}%", couponBonusRate);
        log.info("   최종금리: {}%", calculatedApplyRate);

        // ✅ 예상 이자 재계산 (최종 금리로)
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());

        BigDecimal expectedInterest = productJoinService.calculateExpectedInterest(
                joinRequest.getPrincipalAmount(),
                calculatedApplyRate,  // ✅ 방금 계산한 최종 금리 사용
                joinRequest.getContractTerm(),
                product.getProductType()
        );
        joinRequest.setExpectedInterest(expectedInterest);

        // ✅ 예상 수령액 재계산
        BigDecimal expectedTotal = joinRequest.getPrincipalAmount().add(expectedInterest);
        joinRequest.setExpectedTotal(expectedTotal);

        log.info("✅ STEP 3 처리 완료");
        log.info("   사용 포인트: {} P", usedPoints);
        log.info("   포인트 금리: {}%", pointBonusRate);
        log.info("   선택 쿠폰: {}", selectedCouponId);     // ✅ 쿠폰 로그
        log.info("   쿠폰 금리: {}%", couponBonusRate);     // ✅ 쿠폰 금리 로그
        log.info("   최종 금리: {}%", calculatedApplyRate);
        log.info("   예상 이자: {}원", expectedInterest);
        log.info("   예상 수령액: {}원", expectedTotal);

        // ✅ RedirectAttributes에 명시적으로 추가
        redirectAttributes.addFlashAttribute("joinRequest", joinRequest);

        return "redirect:/prod/productjoin/step4";
    }

// ========================================
// STEP 4: 최종 확인 및 가입 완료
// ========================================

    @GetMapping("/step4")
    public String step4(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 4 진입 - productNo: {}, userNo: {}", joinRequest.getProductNo(), user.getUserNo());

        if (joinRequest.getUserId() == null) {
            joinRequest.setUserId(user.getUserNo());
        }
        if (joinRequest.getUserName() == null) {
            joinRequest.setUserName(user.getUserName());
        }

        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        if (joinRequest.getProductName() == null) {
            joinRequest.setProductName(product.getProductName());
        }
        if (joinRequest.getProductType() == null) {
            joinRequest.setProductType(product.getProductType());
        }

        if (joinRequest.getAccountPassword() == null) {
            joinRequest.setAccountPassword(user.getAccountPassword());
        }

        log.info("✅ STEP 4 준비 완료");
        log.info("   userId: {}, userName: {}", joinRequest.getUserId(), joinRequest.getUserName());
        log.info("   productName: {}, principalAmount: {}", joinRequest.getProductName(), joinRequest.getPrincipalAmount());
        log.info("   사용 포인트: {} P", joinRequest.getUsedPoints());
        log.info("   포인트 금리: {}%", joinRequest.getPointBonusRate());
        log.info("   최종 금리: {}%", joinRequest.getApplyRate());

        return "product/productJoinStage/registerstep04";
    }

    @PostMapping("/complete")
    public String complete(
            @Validated(ProductJoinRequestDTO.Step4.class) @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            BindingResult result,
            @ModelAttribute("user") UsersDTO user,
            SessionStatus sessionStatus,
            Model model) {

        log.info("🚀 최종 가입 완료 처리 시작");
        log.info("   userId: {}", joinRequest.getUserId());
        log.info("   productNo: {}", joinRequest.getProductNo());
        log.info("   principalAmount: {}", joinRequest.getPrincipalAmount());
        log.info("   사용 포인트: {} P", joinRequest.getUsedPoints());
        log.info("   포인트 금리: {}%", joinRequest.getPointBonusRate());
        log.info("   최종 금리: {}%", joinRequest.getApplyRate());
        log.info("   finalAgree: {}", joinRequest.getFinalAgree());

        if (result.hasErrors()) {
            log.error("❌ 최종 동의 검증 실패: {}", result.getAllErrors());
            model.addAttribute("error", "최종 가입 동의가 필요합니다.");
            return step4(joinRequest, user, model);
        }

        if (joinRequest.getUserId() == null) {
            joinRequest.setUserId(user.getUserNo());
        }
        if (joinRequest.getAccountPassword() == null) {
            joinRequest.setAccountPassword(user.getAccountPassword());
        }

        try {
            // ✅ DB INSERT 실행 (선택한 포인트 금리 포함)
            boolean success = productJoinService.processJoin(joinRequest);

            if (success) {
                log.info("✅ 상품 가입 완료!");
                log.info("   저장된 사용 포인트: {} P", joinRequest.getUsedPoints());
                log.info("   저장된 최종 금리: {}%", joinRequest.getApplyRate());

                sessionStatus.setComplete();

                return "redirect:/prod/list/main";

            } else {
                log.error("❌ 가입 처리 실패");
                model.addAttribute("error", "가입 처리 중 오류가 발생했습니다.");
                return step4(joinRequest, user, model);
            }

        } catch (Exception e) {
            log.error("❌ 가입 처리 중 오류 발생", e);
            model.addAttribute("error", "가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return step4(joinRequest, user, model);
        }
    }

    @GetMapping("/success")
    public String success() {
        log.info("✅ 가입 완료 페이지 표시");
        return "/busanbank/prod/list/main";
    }


    // ========================================
    // 기타 유틸리티 메서드
    // ========================================

    /**
     * 약관 PDF 보기용 페이지 (인쇄 최적화)
     * 작성자: 진원, 2025-11-26
     */
    @GetMapping("/term/{termId}")
    public String viewTermPrint(@PathVariable("termId") int termId, Model model) {
        log.info("약관 PDF 보기 - termId: {}", termId);

        // 약관 조회
        ProductTermsDTO term = productTermsService.getTermById(termId);

        if (term == null) {
            log.warn("약관을 찾을 수 없음 - termId: {}", termId);
            return "redirect:/prod/list/main";
        }

        model.addAttribute("term", term);
        return "product/productJoinStage/termPrint";
    }

    /**
     * STEP 4 문서 PDF 보기용 페이지 (상품설명서, 약관, 금리안내)
     * 작성자: 진원, 2025-11-29, termPrint.html 공통 사용
     */
    @GetMapping("/document/{docType}")
    public String viewDocumentPrint(
            @PathVariable("docType") String docType,
            @RequestParam(value = "productNo", required = false) Integer productNo,
            Model model) {
        log.info("문서 PDF 보기 - docType: {}, productNo: {}", docType, productNo);

        String documentTitle = "";
        String documentSubtitle = "";
        String documentContent = "";

        // 문서 타입별 내용 설정
        switch (docType) {
            case "productGuide":
                documentTitle = "상품 설명서";
                documentSubtitle = "BNK 부산은행 정기예금 상품 안내";
                documentContent = generateProductGuideContent(productNo);
                break;

            case "terms":
                documentTitle = "예금거래 기본약관";
                documentSubtitle = "BNK 부산은행";
                documentContent = generateTermsContent();
                break;

            case "rateGuide":
                documentTitle = "금리 안내";
                documentSubtitle = "정기예금 금리 상세 안내";
                documentContent = generateRateGuideContent(productNo);
                break;

            default:
                log.warn("알 수 없는 문서 타입: {}", docType);
                return "redirect:/prod/list/main";
        }

        model.addAttribute("documentTitle", documentTitle);
        model.addAttribute("documentSubtitle", documentSubtitle);
        model.addAttribute("documentContent", documentContent);

        // 작성자: 진원, 2025-11-29, termPrint.html 공통 템플릿 사용
        return "product/productJoinStage/termPrint";
    }

    /**
     * 상품설명서 내용 생성
     * 작성자: 진원, 2025-11-29
     */
    private String generateProductGuideContent(Integer productNo) {
        StringBuilder content = new StringBuilder();
        content.append("<h3>1. 상품 개요</h3>");
        content.append("<p>고객님의 여유 자금을 안전하게 관리하면서 우대금리 혜택을 통해 더 높은 수익을 얻을 수 있는 정기예금 상품입니다.</p>");

        content.append("<h3>2. 가입 대상</h3>");
        content.append("<p>- 실명의 개인 및 개인사업자<br>");
        content.append("- 만 19세 이상 성인<br>");
        content.append("- 내·외국인 모두 가입 가능</p>");

        content.append("<h3>3. 가입 금액 및 기간</h3>");
        content.append("<p>- 최소 가입금액: 100,000원 이상<br>");
        content.append("- 가입 기간: 1개월 ~ 60개월</p>");

        content.append("<h3>4. 우대금리</h3>");
        content.append("<p>- 포인트 금리: 100점당 연 0.1% 추가 금리 제공<br>");
        content.append("- 신규 고객 우대: 최초 가입 시 연 0.3% 추가<br>");
        content.append("- 급여이체 고객: 연 0.2% 추가</p>");

        content.append("<h3>5. 예금자 보호</h3>");
        content.append("<p>이 예금은 예금자보호법에 따라 예금보험공사가 보호하되, 본 은행의 모든 예금보호 대상 금융상품의 원금과 소정의 이자를 합하여 1인당 <strong>최고 5천만원</strong>까지 보호됩니다.</p>");

        content.append("<h3>6. 중도해지</h3>");
        content.append("<p>- 만기 전 중도해지 시 약정이율에서 일정 이율을 차감한 중도해지 이율 적용<br>");
        content.append("- 가입 후 1개월 이내: 연 0.1%<br>");
        content.append("- 가입 후 3개월 이내: 약정이율의 30%<br>");
        content.append("- 가입 후 6개월 이내: 약정이율의 50%<br>");
        content.append("- 가입 후 6개월 초과: 약정이율의 70%</p>");

        content.append("<h3>7. 이자 지급 방법</h3>");
        content.append("<p>- 만기일시지급식: 만기일에 원금과 이자를 일시 지급<br>");
        content.append("- 월이자지급식: 매월 이자만 지급하고 만기일에 원금 지급</p>");

        content.append("<h3>8. 유의사항</h3>");
        content.append("<p>- 이자소득에 대해서는 소득세법에 따라 이자소득세(15.4%)가 원천징수됩니다.<br>");
        content.append("- 만기일이 영업일이 아닌 경우 전 영업일에 만기처리 됩니다.<br>");
        content.append("- 금리는 시장 상황에 따라 변동될 수 있습니다.</p>");

        return content.toString();
    }

    /**
     * 약관 내용 생성
     * 작성자: 진원, 2025-11-29
     */
    private String generateTermsContent() {
        StringBuilder content = new StringBuilder();
        content.append("<h3>제1조 (약관의 적용)</h3>");
        content.append("<p>① 은행과 예금거래를 하는 고객은 이 약관에 따르기로 합니다.<br>");
        content.append("② 이 약관에서 정하지 않은 사항은 관계법령에 따릅니다.</p>");

        content.append("<h3>제2조 (예금계약의 성립)</h3>");
        content.append("<p>예금계약은 예금자가 은행이 정한 예금신청서에 기명날인 또는 서명하고 일정한 금액을 입금함으로써 성립합니다.</p>");

        content.append("<h3>제3조 (이자)</h3>");
        content.append("<p>① 이자는 예금종류별 약정이율에 따라 계산합니다.<br>");
        content.append("② 이자의 지급시기는 예금종류별로 정한 바에 따릅니다.<br>");
        content.append("③ 만기 전 중도해지 시에는 중도해지이율을 적용합니다.</p>");

        content.append("<h3>제4조 (예금자보호)</h3>");
        content.append("<p>이 예금은 예금자보호법에 따라 보호됩니다. 다만, 보호 한도는 1인당 최고 5천만원이며, 초과하는 금액은 보호하지 않습니다.</p>");

        content.append("<h3>제5조 (거래의 제한)</h3>");
        content.append("<p>은행은 예금계좌가 법령에서 정하는 기준을 위반하여 사용되거나 사용될 우려가 있는 경우, 해당 예금계좌의 신규거래를 제한할 수 있습니다.</p>");

        content.append("<h3>제6조 (양도 및 질권설정의 금지)</h3>");
        content.append("<p>① 예금은 타인에게 양도하거나 질권의 목적으로 할 수 없습니다.<br>");
        content.append("② 단, 은행이 별도로 인정하는 경우에는 예외로 합니다.</p>");

        content.append("<h3>제7조 (계좌의 해지)</h3>");
        content.append("<p>① 예금자는 언제든지 이 예금계약을 해지할 수 있습니다.<br>");
        content.append("② 은행은 예금계좌가 법령에 위반되어 이용되거나 1년 이상 거래실적이 없는 경우 예금계약을 해지할 수 있습니다.</p>");

        content.append("<h3>제8조 (면책)</h3>");
        content.append("<p>은행은 다음의 경우 면책됩니다:<br>");
        content.append("① 예금증서, 도장 등을 사용하여 예금을 지급하였을 때<br>");
        content.append("② 예금증서에 기재된 수령인에게 지급하였을 때<br>");
        content.append("③ 불가항력으로 인한 경우</p>");

        return content.toString();
    }

    /**
     * 금리안내 내용 생성
     * 작성자: 진원, 2025-11-29
     */
    private String generateRateGuideContent(Integer productNo) {
        StringBuilder content = new StringBuilder();
        content.append("<h3>1. 기본 금리</h3>");
        content.append("<p>가입 기간에 따라 기본 금리가 차등 적용됩니다.</p>");
        content.append("<p><strong>가입 기간별 기본 금리:</strong><br>");
        content.append("- 1개월 ~ 3개월: 연 2.5%<br>");
        content.append("- 6개월 ~ 9개월: 연 3.0%<br>");
        content.append("- 12개월: 연 3.5%<br>");
        content.append("- 24개월: 연 3.7%<br>");
        content.append("- 36개월 이상: 연 4.0%</p>");

        content.append("<h3>2. 우대금리</h3>");
        content.append("<p><strong>포인트 금리 우대:</strong><br>");
        content.append("- 퀴즈 및 출석체크로 획득한 포인트를 사용하여 금리 우대<br>");
        content.append("- 100점당 연 0.1% 추가 금리 제공<br>");
        content.append("- 최대 연 2.0%까지 우대 가능</p>");

        content.append("<p><strong>기타 우대금리:</strong><br>");
        content.append("- 신규 고객: 연 0.3%<br>");
        content.append("- 급여이체 고객: 연 0.2%<br>");
        content.append("- 자동이체 3건 이상: 연 0.1%<br>");
        content.append("- BNK 부산은행 카드 사용: 연 0.1%</p>");

        content.append("<h3>3. 중도 해지 이율</h3>");
        content.append("<p>만기 전 중도 해지 시 약정이율에서 일정 이율을 차감한 중도해지 이율이 적용됩니다.</p>");
        content.append("<p><strong>경과기간별 중도해지 이율:</strong><br>");
        content.append("- 가입 후 1개월 이내: 연 0.1%<br>");
        content.append("- 가입 후 1개월 초과 ~ 3개월 이내: 약정이율의 30%<br>");
        content.append("- 가입 후 3개월 초과 ~ 6개월 이내: 약정이율의 50%<br>");
        content.append("- 가입 후 6개월 초과: 약정이율의 70%</p>");

        content.append("<h3>4. 만기 후 이율</h3>");
        content.append("<p>만기일 이후 해지하지 않고 보유하는 경우 만기후 이율(연 0.1%)이 적용됩니다.</p>");

        content.append("<h3>5. 금리 적용 기준</h3>");
        content.append("<p>- 금리는 신규 가입일 기준으로 확정됩니다.<br>");
        content.append("- 시장 금리 변동에 따라 금리가 변경될 수 있습니다.<br>");
        content.append("- 이자는 원단위 미만 절사하여 계산합니다.<br>");
        content.append("- 이자소득세 15.4%가 원천징수됩니다.</p>");

        content.append("<h3>6. 주의사항</h3>");
        content.append("<p>- 금리는 세전 금리이며, 실제 수령액은 세후 금액입니다.<br>");
        content.append("- 우대금리는 조건 충족 시에만 적용됩니다.<br>");
        content.append("- 우대조건 미충족 시 기본금리만 적용됩니다.<br>");
        content.append("- 포인트로 적용받은 금리는 가입 시점에 확정되며 변동되지 않습니다.</p>");

        return content.toString();
    }

    /**
     * 이전 단계로 돌아가기
     */
    @GetMapping("/back")
    public String back(@RequestParam("step") int step) {
        return "redirect:/prod/productjoin/step" + (step - 1);
    }

    /**
     * 가입 취소 (Session 초기화)
     */
    @GetMapping("/cancel")
    public String cancel(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/prod/productlist";
    }

    /**
     * 암호화 확인 컨트롤러
     */
    @GetMapping("/test-bcrypt")
    @ResponseBody
    public String testBcrypt() {
        String hash = "$2a$10$59xq/vJmysJykZxzDHUlsOvqGY3g2d4K7WLYKTFPk7PtTCh17PIkS";
        boolean result = passwordEncoder.matches("1111", hash);

        return "BCrypt 비교 결과: " + result;
    }
}