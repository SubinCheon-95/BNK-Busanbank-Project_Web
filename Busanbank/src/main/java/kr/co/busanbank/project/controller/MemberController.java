package kr.co.busanbank.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.project.dto.UsersDTO;
import kr.co.busanbank.project.service.EmailService;
import kr.co.busanbank.project.service.HpService;
import kr.co.busanbank.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/register")
    public String register(UsersDTO usersDTO, HttpServletRequest req) throws Exception {
        log.info(usersDTO.toString());

        Random random = new Random();

        int randomInt = random.nextInt(999999999);

        usersDTO.setUserNo(randomInt);

        log.info("usersDTO = {}", usersDTO);

        memberService.save(usersDTO);

        return "redirect:/member/register/finish";
    }

    @GetMapping("/register/finish")
    public String registerFinish() {
        return "member/registerFinish";
    }

    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }

    @GetMapping("/find/id")
    public String id() {
        return "member/find/id";
    }

    @GetMapping("/find/pw")
    public String pw() {
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

    @GetMapping("/find/pw/result")
    public String pwResult() {
        return "member/find/pwResult";
    }



    // API 요청 메서드
    @ResponseBody
    @GetMapping("/{type}/{value}")
    public ResponseEntity<Map<String, Integer>> getUserCount(@PathVariable("type") String type,
                                                             @PathVariable("value") String value){
        log.info("type = {}, value = {}", type, value);

        int count = memberService.countUser(type, value);


        // Json 생성
        Map<String,Integer> map = Map.of("count", count);

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


}
