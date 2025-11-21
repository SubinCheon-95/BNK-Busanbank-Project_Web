package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.CsDTO;
import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.MemberService;
import kr.co.busanbank.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/my")
public class MyController {

    private final MyService myService;

    @GetMapping("")
    public String index(Model model) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("connectTime", now.format(formatter));


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        int countUserItems = myService.countUserItems(userId);
        model.addAttribute("countUserItems", countUserItems);

        String LastDate = myService.findProductLastDate(userId);
        model.addAttribute("LastDate", LastDate);

        String RecentlyDate = myService.findProductRecentlyDate(userId);
        model.addAttribute("RecentlyDate", RecentlyDate);

        int userNo = myService.findUserNo(userId);

        List<CsDTO> csList = myService.findCsList(userNo);

        for (CsDTO cs : csList) {
            if (cs.getCreatedAt() != null && cs.getCreatedAt().length() >= 10) {
                cs.setCreatedAt(cs.getCreatedAt().substring(0, 10));
            }

        }
        log.info("csList: {}", csList);
        model.addAttribute("csList", csList);


        return "my/index";
    }

    @GetMapping("/items")
    public String items(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("connectTime", now.format(formatter));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        List<UserProductDTO> myproducts = myService.findUserProducts(userId);

        log.info("myproducts = {}", myproducts);

        for (UserProductDTO p : myproducts) {
            if (p.getStartDate() != null && p.getStartDate().length() >= 10) {
                p.setStartDate(p.getStartDate().substring(0, 10));
            }
            if (p.getExpectedEndDate() != null && p.getExpectedEndDate().length() >= 10) {
                p.setExpectedEndDate(p.getExpectedEndDate().substring(0, 10));
            }
        }

        model.addAttribute("myproducts", myproducts);

        return "my/items";
    }



    @GetMapping("/cancel")
    public String cancel(@RequestParam(value = "productNo", required = false) String productNo, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        log.info("cancel page userId = {}", userId);

        List<UserProductDTO> productNames = myService.findUserProductNames(userId);
        model.addAttribute("productNames", productNames);
        log.info("cancel productName List = {}", productNames);

        Integer selectedProductNoInt = null;
        if (productNo != null && !productNo.isEmpty()) {
            selectedProductNoInt = Integer.valueOf(productNo);
        }

        log.info("cancel selectedProductNoInt = {}", selectedProductNoInt);

        model.addAttribute("selectedProductNo", selectedProductNoInt);

        return "my/itemCancel";
    }

    @PostMapping("/cancel")
    public String cancel( @RequestParam("productNo") String productNo) {
        log.info("post cancel productNo: {}", productNo);
        return "redirect:/my/cancel/list?productNo=" + productNo;
    }


    @GetMapping("/cancel/list")
    public String cancelList(@RequestParam("productNo") String productNo, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        UserProductDTO cancelProduct = myService.findCancelProduct(userId, productNo);
        cancelProduct.setStartDate(cancelProduct.getStartDate().substring(0, 10));
        cancelProduct.setExpectedEndDate(cancelProduct.getExpectedEndDate().substring(0, 10));
        log.info("cancel list page cancelProduct: {}", cancelProduct);

        model.addAttribute("cancelProduct", cancelProduct);
        return "my/cancelList";
    }

    @PostMapping("/cancel/list")
    public String cancelList(@RequestParam("userId") String userId,
                           @RequestParam("userPw") String userPw,
                             @RequestParam("productNo") String productNo,
                           Model model) {

        if(!myService.findUserPw(userId, userPw)) {
            model.addAttribute("msg", "비밀번호가 일치하지 않습니다.");
            return "my/withdraw";
        }

        myService.removeProduct(userId,productNo);
        return "redirect:/my/cancel/finish";
    }

    @GetMapping("/cancel/finish")
    public String cancelFinish() {
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






}
