package kr.co.busanbank.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @GetMapping("/register")
    public String register() {
        return "member/register";
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





}
