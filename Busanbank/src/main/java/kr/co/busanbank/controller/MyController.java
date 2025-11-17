package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpSession;
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
        return "my/index";
    }

    @GetMapping("/items")
    public String items(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        model.addAttribute("connectTime", now.format(formatter));
        return "my/items";
    }



    @GetMapping("/cancel")
    public String cancel() {
        return "my/itemCancel";
    }


    @GetMapping("/cancel/list")
    public String cancelList() {
        return "my/cancelList";
    }

    @GetMapping("/cancel/finish")
    public String cancelFinish() {
        return "my/cancelFinish";
    }


    @GetMapping("/modify")
    public String modify(@RequestParam(value="success", required=false) String success,
                         Model model) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        UsersDTO updatedUser = myService.getUserById(userId);

        updatedUser.setUserName(AESUtil.decrypt(updatedUser.getUserName()));
        updatedUser.setHp(AESUtil.decrypt(updatedUser.getHp()));
        updatedUser.setEmail(AESUtil.decrypt(updatedUser.getEmail()));
        updatedUser.setRrn(AESUtil.decrypt(updatedUser.getRrn()));

        // RRN → 생년월일/성별 추출
        String rrn = updatedUser.getRrn();
        if(rrn != null && rrn.length() >= 7){
            String birthPart = rrn.substring(0,6);
            String genderCode = rrn.substring(6,7);
            String yearPrefix = ("1".equals(genderCode) || "2".equals(genderCode)) ? "19" : "20";

            String birthFormatted = yearPrefix + birthPart.substring(0,2) + "-"
                    + birthPart.substring(2,4) + "-"
                    + birthPart.substring(4,6);
            updatedUser.setBirth(birthFormatted);

            String gender = ("1".equals(genderCode) || "3".equals(genderCode)) ? "남성" : "여성";
            updatedUser.setGender(gender);
        }

        if(updatedUser.getHp() != null) {
            String[] hpArr = updatedUser.getHp().split("-");
            if(hpArr.length == 3) {
                model.addAttribute("hp1", hpArr[0]);
                model.addAttribute("hp2", hpArr[1]);
                model.addAttribute("hp3", hpArr[2]);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        // 전역 user를 최신 정보로 갱신
        myUserDetails.setUsersDTO(updatedUser);

        model.addAttribute("user", updatedUser);

        if(success != null){
            model.addAttribute("msg", "회원 정보가 수정되었습니다.");
        }

        return "my/infoModify";
    }

    @PostMapping("/modify")
    public String modify(@RequestParam("userId")  String userId,
                         @RequestParam("email")  String email,
                         @RequestParam("hp1") String hp1,
                         @RequestParam("hp2") String hp2,
                         @RequestParam("hp3") String hp3,
                         @RequestParam("zip") String zip,
                         @RequestParam("addr1") String addr1,
                         @RequestParam("addr2") String addr2,
                         Model model) throws Exception {

        String hp = hp1 + "-" + hp2 + "-" + hp3;
        myService.modifyInfo(userId, email, hp, zip, addr1, addr2);

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









}
