package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.co.busanbank.dto.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 날짜 : 202511/21
 * 이름 : 김수진
 * ***********************************************
 * 내용 :         ProductJoinController
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
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

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

        // 약관 목록 조회
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
     * ✅ 로그인 체크 + 고객 정보 자동 연계
     */
    @GetMapping("/step2")
    public String step2(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 2 진입 - productNo: {}, userNo: {}",
                joinRequest.getProductNo(),
                user != null ? user.getUserNo() : "null");

        // 1. 이전 단계 체크
        if (joinRequest.getProductNo() == null) {
            log.warn("productNo가 없습니다. 상품 목록으로 이동합니다.");
            return "redirect:/prod/list/main";
        }

        // 2. ✅ 로그인 체크
        if (user == null || user.getUserNo() == 0) {
            log.warn("⚠️ 로그인 필요 - 로그인 페이지로 이동");
            model.addAttribute("needLogin", true);
            model.addAttribute("redirectUrl", "/prod/productjoin/step2");
            return "product/productJoinStage/registerstep02";
        }

        // 3. 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        ProductDetailDTO detail = productService.getProductDetail(joinRequest.getProductNo());

        // 4. ✅ 지점 목록 조회
        List<BranchDTO> branches = branchService.getAllBranches();

        // 5. ✅ 고객 정보 자동 설정
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

    /**
     * STEP 2 처리 → STEP 3로 이동
     * ✅ Validation Groups 사용 - STEP 2 검증만 수행
     */
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

        // 0. ✅ STEP 2 필드만 Validation 검증
        if (result.hasErrors()) {
            log.error("입력 검증 실패: {}", result.getAllErrors());
            model.addAttribute("error", "입력 정보를 확인해주세요.");
            return step2(joinRequest, user, model);
        }

        // 1. 비밀번호 확인 검증
        if (joinRequest.getAccountPassword() == null ||
                joinRequest.getAccountPasswordConfirm() == null ||
                !joinRequest.getAccountPassword().equals(joinRequest.getAccountPasswordConfirm())) {
            log.warn("계좌 비밀번호 확인 불일치");
            model.addAttribute("error", "계좌 비밀번호가 일치하지 않습니다.");
            return step2(joinRequest, user, model);
        }

        // 2. ✅ 계좌 비밀번호 DB 비교
        try {
            String inputPassword = joinRequest.getAccountPassword(); // 사용자 입력
            String dbPassword = user.getAccountPassword();           // DB 저장값

            // 🔍 디버깅 로그 추가
            log.info("🔍 비밀번호 비교 시작");
            log.info("   입력값: {}", inputPassword);
            log.info("   DB값 길이: {}", dbPassword != null ? dbPassword.length() : "null");
            if (dbPassword != null && dbPassword.length() > 10) {
                log.info("   DB값 앞 10자: {}", dbPassword.substring(0, 10));
            }

            boolean passwordMatches = false;

            // 🔍 DB 저장 방식 자동 감지
            if (dbPassword == null || dbPassword.isEmpty()) {
                log.error("❌ DB에 계좌 비밀번호가 없음");
                model.addAttribute("error", "계좌 비밀번호가 설정되지 않았습니다.");
                return step2(joinRequest, user, model);

            } else if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$")) {
                // Case 1: BCrypt 암호화
                log.info("📌 BCrypt 방식으로 비교");
                passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);

            } else {
                // Case 2: AES 암호화 또는 평문
                try {
                    String decryptedPassword = AESUtil.decrypt(dbPassword);
                    log.info("📌 AES 복호화 성공, 복호화된 값과 비교");
                    passwordMatches = inputPassword.equals(decryptedPassword);
                } catch (Exception decryptError) {
                    log.info("📌 AES 복호화 실패, 평문으로 비교");
                    passwordMatches = inputPassword.equals(dbPassword);
                }
            }

            // ✅ 비밀번호 불일치 시
            if (!passwordMatches) {
                log.warn("❌ 계좌 비밀번호 DB 비교 실패 - userNo: {}", user.getUserNo());

                // Session 초기화 (중요!)
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

                // 상품 상세 페이지로 redirect
                return "redirect:/prod/view?productNo=" + productNo + "&error=password";
            }

            log.info("✅ 계좌 비밀번호 DB 비교 성공 - userNo: {}", user.getUserNo());

        } catch (Exception e) {
            log.error("계좌 비밀번호 검증 중 오류 발생", e);

            // Session 초기화
            int productNo = joinRequest.getProductNo();
            joinRequest.setProductNo(null);
            joinRequest.setPrincipalAmount(null);
            joinRequest.setContractTerm(null);

            // 상품 상세 페이지로 redirect
            return "redirect:/prod/view?productNo=" + productNo + "&error=system";
        }

        // 3. ✅ 알림 설정 검증
        boolean hasSmsNotification = "Y".equals(joinRequest.getNotificationSms());
        boolean hasEmailNotification = "Y".equals(joinRequest.getNotificationEmail());

        if (!hasSmsNotification && !hasEmailNotification) {
            log.warn("알림 설정 미선택");
            model.addAttribute("error", "만기 알림 설정을 하나 이상 선택해주세요.");
            return step2(joinRequest, user, model);
        }

        // 4. ✅ 알림 인증 검증
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

        // 5. ✅ 가입일 설정 (오늘)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        joinRequest.setStartDate(today);

        // 6. ✅ 예상 만기일 계산
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
     */
    @GetMapping("/step4")
    public String step4(
            @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            @ModelAttribute("user") UsersDTO user,
            Model model) {

        log.info("STEP 4 진입 - productNo: {}", joinRequest.getProductNo());

        if (joinRequest.getProductNo() == null || joinRequest.getApplyRate() == null) {
            return "redirect:/prod/list/main";
        }

        // 상품 정보 조회
        ProductDTO product = productService.getProductById(joinRequest.getProductNo());
        ProductDetailDTO detail = productService.getProductDetail(joinRequest.getProductNo());

        // ✅ joinRequest에 사용자 정보 설정
        joinRequest.setUserId(user.getUserNo());
        joinRequest.setUserName(user.getUserName());
        joinRequest.setProductName(product.getProductName());
        joinRequest.setProductType(product.getProductType());

        model.addAttribute("product", product);
        model.addAttribute("detail", detail);

        log.info("✅ 로그인 사용자: userNo={}, userName={}", user.getUserNo(), user.getUserName());

        return "product/productJoinStage/registerstep04";
    }

    /**
     * 최종 가입 완료 처리
     * ✅ STEP 4 Validation만 수행
     */
    @PostMapping("/complete")
    public String complete(
            @Validated(ProductJoinRequestDTO.Step4.class) @ModelAttribute("joinRequest") ProductJoinRequestDTO joinRequest,
            BindingResult result,
            SessionStatus sessionStatus,
            Model model) {

        log.info("가입 완료 처리 - userId: {}, productNo: {}",
                joinRequest.getUserId(), joinRequest.getProductNo());

        if (result.hasErrors()) {
            log.error("최종 동의 검증 실패: {}", result.getAllErrors());
            model.addAttribute("error", "최종 가입 동의가 필요합니다.");
            return "product/productJoinStage/registerstep04";
        }

        try {
            // 가입 처리
            boolean success = productJoinService.processJoin(joinRequest);

            if (success) {
                // Session 정리
                sessionStatus.setComplete();
                return "redirect:/prod/productjoin/success";
            } else {
                model.addAttribute("error", "가입 처리 중 오류가 발생했습니다.");
                return "product/productJoinStage/registerstep04";
            }

        } catch (Exception e) {
            log.error("가입 처리 중 오류 발생", e);
            model.addAttribute("error", "가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "product/productJoinStage/registerstep04";
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