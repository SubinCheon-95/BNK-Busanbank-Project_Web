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
                        @RequestParam(value = "remaining", required = false) Integer remaining,
                        Model model,
                        HttpSession session) {
        if (error != null) {
            // 비활성 계정 (작성자: 진원, 2025-11-24)
            if ("disabled".equals(error)) {
                model.addAttribute("msg", "계정이 비활성 상태입니다. 최고관리자에게 문의하세요.");
            }
            // 잠긴 계정 (작성자: 진원, 2025-11-24)
            else if ("locked".equals(error)) {
                model.addAttribute("msg", "로그인 실패 횟수 초과로 계정이 잠겼습니다. 관리자에게 문의하세요.");
            }
            // 일반 로그인 실패 (작성자: 진원, 2025-11-20)
            else {
                String msg = "아이디 또는 비밀번호가 잘못되었습니다.";
                if (remaining != null && remaining > 0) {
                    msg += " (남은 시도 횟수: " + remaining + "회)";
                }
                model.addAttribute("msg", msg);
            }
        }
        return "admin/adminLogin";
    }
}

