/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : 마이페이지 기능 처리 컨트롤러 작성
*/
package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.*;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.MemberService;
import kr.co.busanbank.service.MyService;
import kr.co.busanbank.service.UserCouponService;
import kr.co.busanbank.service.goldEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/my")
public class MyController {

    private final MyService myService;
    private final UserCouponService userCouponService;
    private final goldEventService goldEventService;

    @GetMapping({"", "/"})
    public String index(Model model) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("connectTime", now.format(formatter));


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        int userNo = myService.findUserNo(userId);
        String userIdStr = String.valueOf(userNo);

        int countUserItems = myService.countUserItems(userIdStr);
        model.addAttribute("countUserItems", countUserItems);

        String LastDate = myService.findProductLastDate(userIdStr);
        model.addAttribute("LastDate", LastDate);

        String RecentlyDate = myService.findProductRecentlyDate(userIdStr);
        model.addAttribute("RecentlyDate", RecentlyDate);



        List<EmailCounselDTO> csList = myService.findEmailCounseList(userNo);

        for (EmailCounselDTO cs : csList) {
            if (cs.getCreatedAt() != null && cs.getCreatedAt().length() >= 10) {
                cs.setCreatedAt(cs.getCreatedAt().substring(0, 10));
            }

        }
        log.info("csList: {}", csList);
        model.addAttribute("csList", csList);

        double todayPrice = goldEventService.getTodayGoldPrice();
        model.addAttribute("todayPrice", todayPrice);

        // 기본 지급 포인트
        int basePoint = 5000;

        // 지금까지 사용한 포인트 합계
        int usedPoints = myService.getTotalUsedPoints(userIdStr);

        // 현재 남은 포인트 = 5000 - 사용합계
        int remainPoints = basePoint - usedPoints;

        model.addAttribute("remainPoints", remainPoints);

        return "my/index";
    }

    @GetMapping("/items")
    public String items(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("connectTime", now.format(formatter));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        log.info("auth userId: {}", userId);


        int userNo = myService.findUserNo(userId);
        //String userIdStr = String.valueOf(userNo);

        //List<UserProductDTO> myproducts = myService.findUserProducts(userIdStr);

        List<UserAccountDTO> myproducts = myService.findUserAccount(userNo);

        int myBalance = myService.findUserBalance(userNo);
        model.addAttribute("myBalance", myBalance);

        log.info("myproducts = {}", myproducts);

        for (UserAccountDTO p : myproducts) {
            if (p.getStartDate() != null && p.getStartDate().length() >= 10) {
                p.setStartDate(p.getStartDate().substring(0, 10));
            }
            if (p.getExpectedEndDate() != null && p.getExpectedEndDate().length() >= 10) {
                p.setExpectedEndDate(p.getExpectedEndDate().substring(0, 10));
            }
        }

        model.addAttribute("myproducts", myproducts);
        log.info("myproducts = {}", myproducts);

        return "my/items";
    }



    @GetMapping("/cancel")
    public String cancel(@RequestParam(value = "productNo", required = false) String productNo, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        log.info("cancel page userId = {}", userId);

        int userNo = myService.findUserNo(userId);
        String userIdStr = String.valueOf(userNo);

        List<UserProductDTO> productNames = myService.findUserProductNames(userIdStr);
        model.addAttribute("productNames", productNames);
        log.info("cancel productName List = {}", productNames);

        for (UserProductDTO p : productNames) {
            if (p.getExpectedEndDate() != null && p.getExpectedEndDate().length() >= 10) {
                p.setExpectedEndDate(p.getExpectedEndDate().substring(0, 10));
            }
        }

        Integer selectedProductNoInt = null;
        if (productNo != null && !productNo.isEmpty()) {
            selectedProductNoInt = Integer.valueOf(productNo);
        }

        log.info("cancel selectedProductNoInt = {}", selectedProductNoInt);

        model.addAttribute("selectedProductNo", selectedProductNoInt);

        double todayPrice = goldEventService.getTodayGoldPrice();
        model.addAttribute("todayPrice", todayPrice);

        return "my/itemCancel";
    }

    @PostMapping("/cancel")
    public String cancel( @RequestParam("productNo") String productNo) {
        log.info("post cancel productNo: {}", productNo);
        return "redirect:/my/cancel/list?productNo=" + productNo;
    }


    @GetMapping("/cancel/list")
    public String cancelList(@RequestParam("productNo") String productNo, Model model,HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        int userNo = myService.findUserNo(userId);
        //String userIdStr = String.valueOf(userNo);
        int intProductNo = Integer.valueOf(productNo);
        CancelProductDTO cancelProductData  = myService.findCancelProductData(userNo, intProductNo);

        List<UserAccountDTO> depositAccounts = myService.getUserDepositAccounts(userNo);
        model.addAttribute("depositAccounts", depositAccounts);

        LocalDate actualEndDate = LocalDate.now();

        String startDateStr = cancelProductData.getStartDate().split(" ")[0]; // "2025-11-26"
        String endDateStr = cancelProductData.getExpectedEndDate().split(" ")[0];

        log.info("startDateStr: {}", startDateStr);
        log.info("endDateStr: {}", endDateStr);
        log.info("actualEndDate: {}", actualEndDate);

        UserProductDTO upDto = UserProductDTO.builder()
                .productNo(cancelProductData.getProductNo())
                .startDate(startDateStr)
                .expectedEndDate(endDateStr)
                .principalAmount(cancelProductData.getPrincipalAmount())
                .applyRate(BigDecimal.valueOf(cancelProductData.getApplyRate()))
                .contractEarlyRate(BigDecimal.valueOf(cancelProductData.getEarlyTerminateRate()))
                .contractTerm(cancelProductData.getContractTerm())
                .build();

        ProductDTO pDto = ProductDTO.builder()
                .productName(cancelProductData.getProductName())
                .build();

        CancelProductDTO calculated = myService.calculate(upDto, pDto, actualEndDate);

        calculated.setAccountNo(cancelProductData.getAccountNo());
        model.addAttribute("cancelProduct", calculated);

        DecimalFormat df = new DecimalFormat("#,###");
        model.addAttribute("formattedPrincipal", df.format(calculated.getPrincipalAmount()));
        model.addAttribute("formattedEarlyInterest", df.format(calculated.getEarlyInterest()));
        model.addAttribute("formattedMaturityInterest", df.format(calculated.getMaturityInterest()));
        model.addAttribute("formattedRefundInterest", df.format(calculated.getRefundInterest()));
        model.addAttribute("formattedTaxAmount", df.format(calculated.getTaxAmount()));
        model.addAttribute("formattedNetPayment", df.format(calculated.getNetPayment()));
        model.addAttribute("formattedFinalAmount", df.format(calculated.getFinalAmount()));

        calculated.setAccountNo(cancelProductData.getAccountNo());
        calculated.setProductName(cancelProductData.getProductName());

        session.setAttribute("cancelProduct", calculated);

        return "my/cancelList";
    }

    @PostMapping("/cancel/list")
    public String cancelList(@RequestParam("userId") String userId,
                             @RequestParam("accountPassword") String accountPassword,
                             @RequestParam("productNo") String productNo,
                             @RequestParam("accountNo") String depositAccountNo,
                             @RequestParam("finalAmount") double finalAmount,
                             RedirectAttributes redirectAttributes,
                             HttpSession session,
                             Model model) {

        if(!myService.findUserAccountPw(userId, accountPassword)) {
            model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "my/withdraw";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        int userNo = myService.findUserNo(loginId);
        String strUserNo = String.valueOf(userNo);

        CancelProductDTO cancelProduct = (CancelProductDTO) session.getAttribute("cancelProduct");
        String productAccountNo = cancelProduct.getAccountNo();

        // 1. 입금
        myService.depositToAccount(depositAccountNo, (int) finalAmount);

        // 2. 상품 상태를 N으로 변경
        myService.terminateProduct(strUserNo, Integer.parseInt(productNo));

        // 3. 상품 계좌를 비활성
        myService.disableAccount(productAccountNo);

        // 4. Flash 로 전달
        redirectAttributes.addFlashAttribute("cancelProduct", cancelProduct);
        redirectAttributes.addFlashAttribute("accountNo", depositAccountNo);
        redirectAttributes.addFlashAttribute("finalAmount", finalAmount);

        return "redirect:/my/cancel/finish";
    }


    @GetMapping("/cancel/finish")
    public String cancelFinish(@ModelAttribute("cancelProduct") CancelProductDTO cancelProduct,
                               @ModelAttribute("accountNo") String accountNo,
                               @ModelAttribute("finalAmount") double finalAmount,
                               Model model) {

        DecimalFormat df = new DecimalFormat("#,###");

        model.addAttribute("cancelProduct", cancelProduct);
        model.addAttribute("accountNo", accountNo);
        model.addAttribute("formattedPrincipal", df.format(cancelProduct.getPrincipalAmount()));
        model.addAttribute("formattedEarlyInterest", df.format(cancelProduct.getEarlyInterest()));
        model.addAttribute("formattedMaturityInterest", df.format(cancelProduct.getMaturityInterest()));
        model.addAttribute("formattedRefundInterest", df.format(cancelProduct.getRefundInterest()));
        model.addAttribute("formattedTaxAmount", df.format(cancelProduct.getTaxAmount()));
        model.addAttribute("formattedNetPayment", df.format(cancelProduct.getNetPayment()));
        model.addAttribute("formattedFinalAmount", df.format(finalAmount));

        return "my/cancelFinish";
    }



    @GetMapping("/modify")
    public String modify(@RequestParam(value="success", required=false) String success,
                         Model model, HttpSession session) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        UsersDTO user = myService.getUserById(userId);

        UsersDTO updatedUser = processUserData(user);
        updateLoginUser(updatedUser, session);

        // 전화번호 분리
        if(updatedUser.getHp() != null) {
            String[] hpArr = updatedUser.getHp().split("-");
            if(hpArr.length == 3) {
                model.addAttribute("hp1", hpArr[0]);
                model.addAttribute("hp2", hpArr[1]);
                model.addAttribute("hp3", hpArr[2]);
            }
        }

        model.addAttribute("user", updatedUser);

        if(success != null){
            model.addAttribute("msg", "회원 정보가 수정되었습니다.");
        }

        return "my/infoModify";
    }
    @PostMapping("/modify")
    public String modify(
            @RequestParam("userId") String userId,
            @RequestParam("email") String email,
            @RequestParam("hp1") String hp1,
            @RequestParam("hp2") String hp2,
            @RequestParam("hp3") String hp3,
            @RequestParam("zip") String zip,
            @RequestParam("addr1") String addr1,
            @RequestParam("addr2") String addr2,
            HttpSession session
    ) throws Exception {

        String hp = hp1 + "-" + hp2 + "-" + hp3;
        myService.modifyInfo(userId, email, hp, zip, addr1, addr2);

        UsersDTO user = myService.getUserById(userId);
        UsersDTO updatedUser = processUserData(user);

        updateLoginUser(updatedUser, session);

        return "redirect:/my/modify?success=true";
    }



    @GetMapping("/change")
    public String change() {
        return "my/pwModify";
    }

    @PostMapping("/change")
    public String change(@RequestParam("userId") String userId,
                         @RequestParam("pw") String pw,
                         @RequestParam("userPw") String userPw,
                         Model model) {

        boolean isCorrect = myService.findUserPw(userId, pw);

        if(isCorrect){
            myService.modifyPw(userId, userPw);
            model.addAttribute("msg", "비밀번호가 수정되었습니다.");
        } else {
            model.addAttribute("msg", "현재 비밀번호가 일치하지 않습니다.");
        }

        return "my/pwModify";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "my/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("userId") String userId,
                           @RequestParam("userPw") String userPw,
                           HttpSession session,
                           Model model) {

        if(!myService.findUserPw(userId, userPw)) {
            model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "my/withdraw";
        }

        myService.withdrawUser(userId);
        session.invalidate();
        return "redirect:/member/withdraw/finish";
    }




    private UsersDTO processUserData(UsersDTO user) throws Exception {

        user.setUserName(AESUtil.decrypt(user.getUserName()));
        user.setHp(AESUtil.decrypt(user.getHp()));
        user.setEmail(AESUtil.decrypt(user.getEmail()));
        user.setRrn(AESUtil.decrypt(user.getRrn()));

        String rrn = user.getRrn();
        if (rrn != null && rrn.length() >= 7) {
            String birthPart = rrn.substring(0,6);
            String genderCode = rrn.substring(6,7);

            String yearPrefix = ("1".equals(genderCode) || "2".equals(genderCode)) ? "19" : "20";

            String birthFormatted = yearPrefix + birthPart.substring(0,2) + "-"
                    + birthPart.substring(2,4) + "-"
                    + birthPart.substring(4,6);
            user.setBirth(birthFormatted);

            String gender = ("1".equals(genderCode) || "3".equals(genderCode)) ? "남성" : "여성";
            user.setGender(gender);
        }

        return user;
    }

    private void updateLoginUser(UsersDTO updatedUser, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        UsersDTO oldUser = myUserDetails.getUsersDTO();

        updatedUser.setRegDays(oldUser.getRegDays());
        updatedUser.setRegDate(oldUser.getRegDate());

        session.setAttribute("decryptedUser", updatedUser);

        myUserDetails.setUsersDTO(updatedUser);

    }

    /**
     * 작성자: 진원
     * 작성일: 2025-11-28
     * 설명: 쿠폰 등록 페이지
     */
    @GetMapping("/coupon")
    public String coupon(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        int userNo = myService.findUserNo(userId);

        // 보유 쿠폰 목록 조회
        List<UserCouponDTO> coupons = userCouponService.getUserCoupons(userNo);
        model.addAttribute("coupons", coupons);

        // 사용 가능한 쿠폰 개수
        int availableCount = userCouponService.getAvailableCouponCount(userNo);
        model.addAttribute("availableCount", availableCount);

        // 사용한 쿠폰 개수
        int usedCount = userCouponService.getUsedCouponCount(userNo);
        model.addAttribute("usedCount", usedCount);

        // Footer를 위한 appInfo (null이어도 무방)
        model.addAttribute("appInfo", null);

        log.info("쿠폰 페이지 로드 - userNo: {}, 보유: {}, 사용가능: {}, 사용완료: {}",
                userNo, coupons.size(), availableCount, usedCount);

        return "my/coupon";
    }

    /**
     * 작성자: 진원
     * 작성일: 2025-11-28
     * 설명: 쿠폰 등록 처리
     */
    @PostMapping("/coupon/register")
    @ResponseBody
    public Map<String, Object> registerCoupon(@RequestParam("couponCode") String couponCode) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = auth.getName();
            int userNo = myService.findUserNo(userId);

            String result = userCouponService.registerCoupon(userNo, couponCode);

            if ("SUCCESS".equals(result)) {
                response.put("success", true);
                response.put("message", "쿠폰이 성공적으로 등록되었습니다!");
            } else {
                response.put("success", false);
                response.put("message", result);
            }

        } catch (Exception e) {
            log.error("쿠폰 등록 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 등록 중 오류가 발생했습니다.");
        }

        return response;
    }


    @PostMapping("/event/gold")
    public ResponseEntity<?> goldPick(Authentication auth) {

        String userId = auth.getName();
        int userNo = myService.findUserNo(userId);

        GoldEventLogDTO today = goldEventService.findTodayEvent(userNo);

        if (today != null) {
            return ResponseEntity.ok(Map.of(
                    "already", true
            ));
        }

        // ---- 오늘 기록 없으면 무조건 참여 가능 ----

        double errorRate = getRandomRange();
        double todayPrice = goldEventService.getTodayGoldPrice();

        double errorAmount = todayPrice * (errorRate / 100);
        double min = todayPrice - errorAmount;
        double max = todayPrice + errorAmount;

        goldEventService.saveEvent(userId, todayPrice, errorRate, min, max);

        return ResponseEntity.ok(Map.of(
                "already", false,
                "errorRate", errorRate,
                "errorAmount", errorAmount,
                "min", min,
                "max", max,
                "todayPrice", todayPrice
        ));
    }


    public double getRandomRange() {

        double random = Math.random();

        if(random < 0.5) {
            return 0.3;
        } else if(random < 0.8) {
            return 0.2;
        } else {
            return 0.1;
        }
    }

    @GetMapping("/event/status")
    @ResponseBody
    public Map<String, Object> getEventStatus(Authentication auth) {

        int userNo = myService.findUserNo(auth.getName());

        GoldEventLogDTO today = goldEventService.findTodayEvent(userNo);
        GoldEventLogDTO last = goldEventService.findLastEvent(userNo);

        // ------------------------------
        // CASE 1: 오늘 기록이 있음
        // ------------------------------
        if (today != null) {
            double errorAmount = today.getTodayPrice() * (today.getErrorRate() / 100);

            return Map.of(
                    "todayStatus", today.getResult(),   // WAIT / FAIL / SUCCESS
                    "pastStatus", (last != null ? last.getResult() : "NONE"),
                    "errorRate", today.getErrorRate(),
                    "minPrice", today.getMinPrice(),
                    "maxPrice", today.getMaxPrice(),
                    "todayPrice", today.getTodayPrice(),
                    "errorAmount", errorAmount
            );
        }

        // ------------------------------
        // CASE 2: 오늘 기록 없음 → 과거 기록 기준 처리
        // ------------------------------
        if (last != null) {

            // 2-1 과거 SUCCESS → 오늘 기록 없어도 SUCCESS 유지, 재도전 불가
            if ("SUCCESS".equals(last.getResult())) {
                return Map.of(
                        "todayStatus", "NONE",
                        "pastStatus", "SUCCESS",
                        "minPrice", last.getMinPrice(),
                        "maxPrice", last.getMaxPrice(),
                        "todayPrice", goldEventService.getTodayGoldPrice()
                );
            }

            // 2-2 과거 FAIL → FAIL UI는 보여주고 오늘은 재도전 가능
            if ("FAIL".equals(last.getResult())) {
                return Map.of(
                        "todayStatus", "NONE",
                        "pastStatus", "FAIL",
                        "minPrice", last.getMinPrice(),
                        "maxPrice", last.getMaxPrice(),
                        "todayPrice", goldEventService.getTodayGoldPrice()
                );
            }
        }

        // ------------------------------
        // CASE 3: 완전 신규 사용자
        // ------------------------------
        return Map.of(
                "todayStatus", "NONE",
                "pastStatus", "NONE",
                "todayPrice", goldEventService.getTodayGoldPrice()
        );
    }


}
