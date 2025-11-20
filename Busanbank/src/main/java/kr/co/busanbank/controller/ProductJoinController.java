package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.co.busanbank.dto.*;
import kr.co.busanbank.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** ***********************************************
 *             ProductJoinController
 ************************************************ */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/prod/productjoin")
@SessionAttributes("joinRequest")  // Session에 저장
public class ProductJoinController {

    private final ProductService productService;
    private final ProductTermsService productTermsService;
    private final ProductJoinService productJoinService;
    private final BranchService branchService;

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

        // 상품 정보 조회
        ProductDTO product = productService.getProductById(productNo);
        ProductDetailDTO detail = productService.getProductDetail(productNo);

        // 약관 목록 조회 (STEP 1에 표시할 약관만)
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

        // 필수 약관 동의 검증
        if (!productTermsService.validateRequiredTerms(productNo, agreedTermIds)) {
            model.addAttribute("error", "모든 필수 약관에 동의해주세요.");
            return step1(productNo, model);
        }

        // Session에 저장
        joinRequest.setProductNo(productNo);
        joinRequest.setAgreedTermIds(agreedTermIds);

        return "redirect:/prod/productjoin/step2";
    }

    // ========================================
    // STEP 2: 정보 입력
    // ========================================

    /**
     * STEP 2: 정보 입력 페이지
     * 로그인 체크 + 고객 정보 자동 연계
     */
    @GetMapping("/step2")
    public String step2(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,  // GlobalController에서 자동 주입
            Model model) {

        log.info("STEP 2 진입 - productNo: {}, userNo: {}",
                joinRequest.getProductNo(),
                user != null ? user.getUserNo() : "null");

        // 1. 이전 단계 체크
        if (joinRequest.getProductNo() == null) {
            log.warn("productNo가 없습니다. 상품 목록으로 이동합니다.");
            return "redirect:/prod/list/main";
        }

        // 2. ✅ 로그인 체크 (GlobalController에서 user가 비어있으면 미로그인)
        if (user == null || user.getUserNo() == 0) {
            log.warn("⚠️ 로그인 필요 - 로그인 페이지로 이동");
            model.addAttribute("needLogin", true);
            model.addAttribute("redirectUrl", "/prod/productjoin/step2");
            // JavaScript에서 alert + redirect 처리
            return "product/productJoinStage/registerstep02";
        }

        // 3. 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        ProductDetailDTO detail = productService.getProductDetail(joinRequest.getProductNo());

        // 4. ✅ 지점 목록 조회
        List<BranchDTO> branches = branchService.getAllBranches();

        // 5. ✅ 고객 정보 자동 설정 (GlobalController에서 이미 복호화됨!)
        model.addAttribute("product", product);
        model.addAttribute("detail", detail);
        model.addAttribute("branches", branches);
        model.addAttribute("userName", user.getUserName());     // 이름
        model.addAttribute("userHp", user.getHp());             // 휴대폰
        model.addAttribute("userEmail", user.getEmail());       // 이메일

        log.info("✅ 고객 정보 연계 완료: 이름={}, 휴대폰={}, 이메일={}",
                user.getUserName(), user.getHp(), user.getEmail());

        return "product/productJoinStage/registerstep02";
    }

    /**
     * STEP 2 처리 → STEP 3로 이동
     * 지점/직원 선택 + 알림 인증 검증
     */
    @PostMapping("/step2")
    public String processStep2(
            @Valid @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,  // 추가
            BindingResult result,
            Model model) {

        log.info("STEP 2 처리 - principalAmount: {}, contractTerm: {}, branchId: {}, empId: {}, smsVerified: {}, emailVerified: {}",
                joinRequest.getPrincipalAmount(),
                joinRequest.getContractTerm(),
                joinRequest.getBranchId(),
                joinRequest.getEmpId(),
                joinRequest.getSmsVerified(),
                joinRequest.getEmailVerified());

        if (result.hasErrors()) {
            log.error("입력 검증 실패: {}", result.getAllErrors());
            return step2(joinRequest, user, model);
        }

        // 1. 비밀번호 확인 검증
        if (!joinRequest.getAccountPassword().equals(joinRequest.getAccountPasswordConfirm())) {
            model.addAttribute("error", "계좌 비밀번호가 일치하지 않습니다.");
            return step2(joinRequest, user, model);
        }

        // 2. ✅ 알림 설정 검증 (SMS 또는 이메일 중 하나는 필수)
        boolean hasSmsNotification = "Y".equals(joinRequest.getNotificationSms());
        boolean hasEmailNotification = "Y".equals(joinRequest.getNotificationEmail());

        if (!hasSmsNotification && !hasEmailNotification) {
            model.addAttribute("error", "만기 알림 설정을 하나 이상 선택해주세요.");
            return step2(joinRequest, user, model);
        }

        // 3. ✅ 알림 인증 검증
        if (hasSmsNotification && !Boolean.TRUE.equals(joinRequest.getSmsVerified())) {
            model.addAttribute("error", "SMS 인증을 완료해주세요.");
            return step2(joinRequest, user, model);
        }

        if (hasEmailNotification && !Boolean.TRUE.equals(joinRequest.getEmailVerified())) {
            model.addAttribute("error", "이메일 인증을 완료해주세요.");
            return step2(joinRequest, user, model);
        }

        // 4. 가입일 설정 (오늘)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        joinRequest.setStartDate(today);

        // 5. 예상 만기일 계산
        String expectedEndDate = productJoinService.calculateExpectedEndDate(
                today, joinRequest.getContractTerm());
        joinRequest.setExpectedEndDate(expectedEndDate);

        log.info("✅ STEP 2 처리 완료 - 가입일: {}, 만기일: {}", today, expectedEndDate);

        return "redirect:/prod/productjoin/step3";
    }


    // ========================================
    // STEP 3: 금리 확인
    // ========================================

    /**
     * STEP 3: 금리 확인 페이지
     */
    @GetMapping("/step3")
    public String step3(@ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest, Model model) {
        log.info("STEP 3 진입 - productNo: {}", joinRequest.getProductNo());

        if (joinRequest.getProductNo() == null || joinRequest.getPrincipalAmount() == null) {
            return "redirect:/prod/list/main";
        }

        // 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());

        // 적용 금리 계산
        BigDecimal applyRate = productJoinService.calculateApplyRate(joinRequest.getProductNo());
        joinRequest.setApplyRate(applyRate);
        joinRequest.setBaseRate(product.getBaseRate());
        joinRequest.setEarlyTerminateRate(product.getEarlyTerminateRate());

        // 예상 이자 계산
        BigDecimal expectedInterest = productJoinService.calculateExpectedInterest(
                joinRequest.getPrincipalAmount(),
                applyRate,
                joinRequest.getContractTerm(),
                product.getProductType()
        );
        joinRequest.setExpectedInterest(expectedInterest);

        // 예상 수령액 계산
        BigDecimal expectedTotal = joinRequest.getPrincipalAmount().add(expectedInterest);
        joinRequest.setExpectedTotal(expectedTotal);

        model.addAttribute("product", product);

        return "product/productJoinStage/registerstep03";
    }

    /**
     * STEP 3 처리 → STEP 4로 이동
     */
    @PostMapping("/step3")
    public String processStep3(@ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest) {
        log.info("STEP 3 처리 완료 - applyRate: {}", joinRequest.getApplyRate());
        return "redirect:/prod/productjoin/step4";
    }

    // ========================================
    // STEP 4: 최종 확인 및 가입 완료
    // ========================================
    /**
     * STEP 4: 최종 확인 페이지
     * GlobalController에서 @ModelAttribute("user")로 이미 복호화된 user 객체를 제공하므로
     * 세션에서 직접 가져올 필요 없이 Model에서 가져오면 됨!
     */
    @GetMapping("/step4")
    public String step4(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,  // GlobalController에서 자동 주입!
            Model model) {

        log.info("STEP 4 진입 - productNo: {}", joinRequest.getProductNo());

        if (joinRequest.getProductNo() == null || joinRequest.getApplyRate() == null) {
            return "redirect:/prod/list/main";
        }

        // ✅ 로그인 체크 (비로그인 시 UserNo = 0 객체가 비어있으면 비로그인)
        //if (user == null || user.getUserNo() == 0) {
        //    log.warn("⚠️ 로그인 필요 - 로그인 페이지로 이동");
        //    return "redirect:/member/login?redirect_uri=/prod/productjoin/step4";
        //}

        // 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        ProductDetailDTO detail = productService.getProductDetail(joinRequest.getProductNo());

        // ✅ joinRequest에 사용자 정보 설정 (GlobalController에서 이미 복호화됨!)
        joinRequest.setUserId(user.getUserNo());     // userNo 사용
        joinRequest.setUserName(user.getUserName()); // 이미 복호화된 이름
        joinRequest.setProductName(product.getProductName());
        joinRequest.setProductType(product.getProductType());

        model.addAttribute("product", product);
        model.addAttribute("detail", detail);

        log.info("✅ 로그인 사용자: userNo={}, userName={}", user.getUserNo(), user.getUserName());

        return "product/productJoinStage/registerstep04";
    }


    /**
     * 최종 가입 완료 처리
     */
    @PostMapping("/complete")
    public String complete(
            @Valid @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            BindingResult result,
            SessionStatus sessionStatus,
            Model model) {

        log.info("가입 완료 처리 - userId: {}, productNo: {}",
                joinRequest.getUserId(), joinRequest.getProductNo());

        if (result.hasErrors()) {
            return step4(joinRequest, null, model);
        }

        // 최종 동의 확인
        if (joinRequest.getFinalAgree() == null || !joinRequest.getFinalAgree()) {
            model.addAttribute("error", "최종 가입 동의가 필요합니다.");
            return step4(joinRequest, null, model);
        }

        try {
            // 가입 처리
            boolean success = productJoinService.processJoin(joinRequest);

            if (success) {
                // Session 정리
                sessionStatus.setComplete();

                // 성공 페이지로 이동
                return "redirect:/prod/productjoin/success";
            } else {
                model.addAttribute("error", "가입 처리 중 오류가 발생했습니다.");
                return step4(joinRequest, null, model);
            }

        } catch (Exception e) {
            log.error("가입 처리 중 오류 발생", e);
            model.addAttribute("error", "가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return step4(joinRequest, null, model);
        }
    }

    /**
     * 가입 완료 성공 페이지
     */
    @GetMapping("/success")
    public String success() {
        return "product/productJoinStage/success";
    }

    // ========================================
    // 기타 유틸리티 메서드
    // ========================================

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