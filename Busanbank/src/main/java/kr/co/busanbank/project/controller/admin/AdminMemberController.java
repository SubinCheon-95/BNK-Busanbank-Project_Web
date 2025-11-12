package kr.co.busanbank.project.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@Controller
public class AdminMemberController {

    @GetMapping("/list")
    public String list(Model model) {return "admin/member/adminMemberList";}
}
