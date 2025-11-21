/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : 회원 기능 처리 컨트롤러 작성
*/
package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.EmailService;
import kr.co.busanbank.service.HpService;
import kr.co.busanbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final HpService hpService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "redirect_uri", required = false) String redirectUri,
                        @RequestParam(value = "error", required = false) String error,
                        Model model,
                        HttpSession session) {
        if (redirectUri != null) {
            session.setAttribute("redirect_uri", redirectUri);
        }
        if (error != null) {
            model.addAttribute("msg", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        return "member/login";
    }

    @GetMapping("/register")
    public String register() {
        return "member/register";
    }

    /**
     * 회원가입 처리
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PostMapping("/register")
    public String register(UsersDTO usersDTO, HttpServletRequest req, Model model) throws Exception {
        log.info(usersDTO.toString());

        try {
            Random random = new Random();
            int randomInt = random.nextInt(999999999);
            usersDTO.setUserNo(randomInt);

            log.info("usersDTO = {}", usersDTO);

            memberService.save(usersDTO);

            return "redirect:/member/register/finish";
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반
            log.warn("회원가입 실패 - 비밀번호 정책 위반: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usersDTO", usersDTO);
            return "member/register";
        }
    }

    @GetMapping("/register/finish")
    public String registerFinish() {
        return "member/registerFinish";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        List<TermDTO> terms = memberService.findTermsAll();
        log.info("terms = {}", terms);
        model.addAttribute("terms", terms);
        return "member/signup";
    }

    @GetMapping("/find/id")
    public String userId(){
        return "member/find/id";
    }


    @PostMapping("/find/id")
    public String id(@RequestParam("authMethod") int authMethod,
                     String userName,
                     @RequestParam(value = "email", required = false) String email,
                     @RequestParam(value = "hp", required = false) String hp,
                     Model model) throws Exception {

        log.info("userName: {}, email: {}, hp: {}", userName, email, hp);
        if(authMethod == 1){
            UsersDTO findIdInfo = memberService.getUserIdInfoEmail(userName, email);
            if(findIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/id";
            } else {
                findIdInfo.setUserName(AESUtil.decrypt(findIdInfo.getUserName()));
                findIdInfo.setEmail(AESUtil.decrypt(findIdInfo.getEmail()));
                model.addAttribute("findIdInfo", findIdInfo);
                return "member/find/idResult";
            }
        }else if(authMethod == 2){
            UsersDTO finIdInfo = memberService.getUserIdInfoHp(userName, hp);
            if(finIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/id";
            }else{
                finIdInfo.setUserName(AESUtil.decrypt(finIdInfo.getUserName()));
                finIdInfo.setHp(AESUtil.decrypt(finIdInfo.getHp()));
                model.addAttribute("findIdInfo", finIdInfo);
                return "member/find/idResult";
            }
        }
        return "member/find/id";
    }

    @GetMapping("/find/pw")
    public String pw() {
        return "member/find/pw";
    }

    @PostMapping("/find/pw")
    public String pw(@RequestParam("authMethod") int authMethod,
                     String userName,
                     String userId,
                     @RequestParam(value = "email", required = false) String email,
                     @RequestParam(value = "hp", required = false) String hp,
                     Model model) throws Exception {

        log.info("userName: {}, userId: {}, email: {}, hp: {}", userName, userId, email, hp);
        if(authMethod == 1){
            UsersDTO findIdInfo = memberService.getUserPwInfoEmail(userName, userId, email);
            if(findIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/pw";
            } else {
                findIdInfo.setUserName(AESUtil.decrypt(findIdInfo.getUserName()));
                findIdInfo.setEmail(AESUtil.decrypt(findIdInfo.getEmail()));
                model.addAttribute("findIdInfo", findIdInfo);
                return "member/find/changePw";
            }
        }else if(authMethod == 2){
            UsersDTO finIdInfo = memberService.getUserPwInfoHp(userName, userId, hp);
            if(finIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/pw";
            }else{
                finIdInfo.setUserName(AESUtil.decrypt(finIdInfo.getUserName()));
                finIdInfo.setHp(AESUtil.decrypt(finIdInfo.getHp()));
                model.addAttribute("findIdInfo", finIdInfo);
                return "member/find/changePw";
            }
        }
        return "member/find/pw";
    }



    @GetMapping("/find/id/result")
    public String idResult() {
        return "member/find/idResult";
    }

    @GetMapping("/find/pw/change")
    public String changePw() {
        return "member/find/changePw";
    }

    /**
     * 비밀번호 변경 처리
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PostMapping("/find/pw/change")
    public String changePw(@RequestParam("userId") String userId,
                           @RequestParam("userPw") String userPw,
                           Model model) {
        log.info("userId: {}, userPw: {}", userId, userPw);

        try {
            memberService.modifyPw(userId, userPw);
            return "redirect:/member/find/pw/result";
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반
            log.warn("비밀번호 변경 실패 - 비밀번호 정책 위반: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", userId);
            return "member/find/changePw";
        }
    }



    @GetMapping("/find/pw/result")
    public String pwResult() {
        return "member/find/pwResult";
    }



    // API 요청 메서드
    @ResponseBody
    @GetMapping("/{type}/{value}")
    public ResponseEntity<Map<String, Integer>> getUserCount(@PathVariable("type") String type,
                                                             @PathVariable("value") String value) throws Exception {
        log.info("type = {}, value = {}", type, value);

        String queryValue;
        if ("userId".equals(type)) {
            queryValue = value;
        } else {
            queryValue = AESUtil.encrypt(value); // 암호화
        }

        int count = memberService.countUser(type, queryValue);

        // Json 생성
        Map<String, Integer> map = Map.of("count", count);
        return ResponseEntity.ok(map);
    }


    @PostMapping("/email/send")
    @ResponseBody
    public ResponseEntity<String> sendEmail(@RequestBody Map<String,String> req){
        String email = req.get("email");
        int count = memberService.countUser("email", email);

        if(count > 0){
            return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
        }else{
            emailService.sendCode(email); // 조건 맞으면 발송
            return ResponseEntity.ok("인증 코드 발송 완료");
        }
    }

    @PostMapping("/hp/send")
    @ResponseBody
    public ResponseEntity<String> sendHp(@RequestBody Map<String,String> req) {
        String hp = req.get("hp");
        String mode = req.get("mode"); // "join" 또는 "find"
        int count = memberService.countUser("hp", hp);

        if("join".equals(mode) && count > 0){
            return ResponseEntity.badRequest().body("이미 존재하는 휴대폰입니다..");
        }

        if("find".equals(mode) && count == 0){
            return ResponseEntity.badRequest().body("존재하지 않는 휴대폰입니다.");
        }
        hpService.sendCode(hp); // 조건 맞으면 발송
        return ResponseEntity.ok("인증 코드 발송 완료");
    }

    @GetMapping("/withdraw/finish")
    public String withdrawFinish() {
        return "member/withdrawFinish";
    }

    @GetMapping("/auto")
    public String auto() {
        return "member/autoLogout";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "member/chatbotTest";
    }


    /**
     *  상품 가입용 SMS/이메일 인증 검증 API
     */
    @PostMapping("/hp/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyHp(@RequestBody Map<String,String> req) {
        String code = req.get("code");
        boolean verified = hpService.verifyCode(code);

        Map<String, Boolean> result = Map.of("verified", verified);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@RequestBody Map<String,String> req) {
        String code = req.get("code");
        boolean verified = emailService.verifyCode(code);

        Map<String, Boolean> result = Map.of("verified", verified);
        return ResponseEntity.ok(result);
    }


}

