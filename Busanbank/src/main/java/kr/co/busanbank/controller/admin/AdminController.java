package kr.co.busanbank.controller.admin;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("")
    public String main() {
        return "admin/adminMain";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model,
                        HttpSession session) {
        if (error != null) {
            model.addAttribute("msg", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        return "admin/adminLogin";
    }
}

