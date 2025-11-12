package kr.co.busanbank.project.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor /*파일명 _ 바꾸기 */
@RequestMapping("/admin")
@Controller
public class AdminMainController {

    @GetMapping("")
    public String index(Model model) {return "admin/adminMain";}

    @GetMapping("/login")
    public String login(Model model) {return "admin/adminLogin";}
}

