package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.co.busanbank.dto.*;
import kr.co.busanbank.dto.quiz.UserStatusDTO;
import kr.co.busanbank.entity.quiz.UserLevel;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        // 계좌 비밀번호 DB 비교 로직 (기존 코드 유지)
        try {
            String inputPassword = joinRequest.getAccountPassword();
            String dbPassword = user.getAccountPassword();

            log.info("🔍 비밀번호 비교 시작");
            log.info("   입력값: {}", inputPassword);
            log.info("   DB값 길이: {}", dbPassword != null ? dbPassword.length() : "null");
            if (dbPassword != null && dbPassword.length() > 10) {
                log.info("   DB값 앞 10자: {}", dbPassword.substring(0, 10));
            }

            boolean passwordMatches = false;

            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ DB에 계좌 비밀번호가 없음");
                model.addAttribute("error", "계좌 비밀번호가 설정되지 않았습니다.");
                return step2(joinRequest, user, model);

            } else if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$")) {
                log.info("📌 BCrypt 방식으로 비교");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);

            } else {
                try {
                    String decryptedPassword = AESUtil.decrypt(dbPassword);
                    log.info("📌 AES 복호화 성공, 복호화된 값과 비교");
                    passwordMatches = inputPassword.equals(decryptedPassword);
                } catch (Exception decryptError) {
                    log.info("📌 AES 복호화 실패, 평문으로 비교");
                    passwordMatches = inputPassword.equals(dbPassword);
                }
            }

            if (!passwordMatches) {
                log.warn("❌ 계좌 비밀번호 DB 비교 실패 - userNo: {}", user.getUserNo());

                int productNo = joinRequest.getProductNo();
                joinRequest.setProductNo(null);
                joinRequest.setPrincipalAmount(null);
                joinRequest.setContractTerm(null);
                joinRequest.setAccountPassword(null);
                joinRequest.setAccountPasswordConfirm(null);
                joinRequest.setBranchId(null);
                joinRequest.setEmpId(null);
                joinRequest.setNotificationSms(null);
                joinRequest.setNotificationEmail(null);
                joinRequest.setSmsVerified(false);
                joinRequest.setEmailVerified(false);

                return "redirect:/prod/view?productNo=" + productNo + "&error=password";
            }

            log.info("✅ 계좌 비밀번호 DB 비교 성공 - userNo: {}", user.getUserNo());

        } catch (Exception e) {
            log.error("계좌 비밀번호 검증 중 오류 발생", e);

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

    // ========================================
    // STEP 3: 금리 확인 (✅ 포인트 금리 추가!)
    // ========================================

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
        int userPoints = 0;
        BigDecimal pointBonusRate = BigDecimal.ZERO;

        try {
            Optional<UserLevel> userLevelOpt = userLevelRepository.findByUserId(Long.valueOf(user.getUserNo()));

            if (userLevelOpt.isPresent()) {
                UserLevel userLevel = userLevelOpt.get();
                userPoints = userLevel.getTotalPoints() != null ? userLevel.getTotalPoints() : 0;

                // 100점당 0.1% 금리 추가
                pointBonusRate = BigDecimal.valueOf(userPoints)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .multiply(BigDecimal.valueOf(0.1))
                        .setScale(2, RoundingMode.HALF_UP);

                log.info("✅ 포인트 금리 계산 완료");
                log.info("   사용자 포인트: {}", userPoints);
                log.info("   포인트 금리: {}%", pointBonusRate);
            } else {
                log.warn("⚠️ 사용자 레벨 정보 없음 - userNo: {}", user.getUserNo());
            }

        } catch (Exception e) {
            log.error("❌ 포인트 조회 실패", e);
        }

        // ✅ 3. 최종 금리 = 기본 금리 + 포인트 금리
        BigDecimal finalApplyRate = applyRate.add(pointBonusRate);

        // ✅ 4. Session에 저장
        joinRequest.setBaseRate(baseRate);
        joinRequest.setApplyRate(finalApplyRate);
        joinRequest.setPointBonusRate(pointBonusRate);
        joinRequest.setUserPoints(userPoints);
        joinRequest.setEarlyTerminateRate(product.getEarlyTerminateRate());

        // ✅ 5. 예상 이자 계산 (최종 금리로 계산)
        BigDecimal expectedInterest = productJoinService.calculateExpectedInterest(
                joinRequest.getPrincipalAmount(),
                finalApplyRate,
                joinRequest.getContractTerm(),
                product.getProductType()
        );
        joinRequest.setExpectedInterest(expectedInterest);

        // ✅ 6. 예상 수령액 계산
        BigDecimal expectedTotal = joinRequest.getPrincipalAmount().add(expectedInterest);
        joinRequest.setExpectedTotal(expectedTotal);

        // ✅ 7. Model에 추가
        model.addAttribute("product", product);
        model.addAttribute("userPoints", userPoints);
        model.addAttribute("pointBonusRate", pointBonusRate);

        log.info("✅ STEP 3 준비 완료");
        log.info("   기본 금리: {}%", baseRate);
        log.info("   포인트 금리: {}%", pointBonusRate);
        log.info("   최종 금리: {}%", finalApplyRate);
        log.info("   예상 이자: {}원", expectedInterest);

        return "product/productJoinStage/registerstep03";
    }

    @PostMapping("/step3")
    public String processStep3(@ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest) {
        log.info("STEP 3 처리 완료");
        log.info("   contractTerm: {}", joinRequest.getContractTerm());
        log.info("   applyRate: {}", joinRequest.getApplyRate());
        log.info("   pointBonusRate: {}", joinRequest.getPointBonusRate());
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
        log.info("   최종 금리: {}%", joinRequest.getApplyRate());
        log.info("   포인트 금리: {}%", joinRequest.getPointBonusRate());

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
            // ✅ DB INSERT 실행 (포인트 금리 포함된 applyRate로 저장됨)
            boolean success = productJoinService.processJoin(joinRequest);

            if (success) {
                log.info("✅ 상품 가입 완료!");

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
        return "product/productJoinStage/success";
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
}