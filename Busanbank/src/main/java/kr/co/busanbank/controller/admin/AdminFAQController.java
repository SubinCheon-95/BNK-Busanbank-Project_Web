package kr.co.busanbank.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
@Controller
public class AdminFAQController {

    @GetMapping("/list")
    public String list(Model model) {return "admin/cs/faq/admin_FAQList";}

    @GetMapping("/write")
    public String write(Model model) {return "admin/cs/faq/admin_FAQWrite";}

    @GetMapping("/modify")
    public String modify(Model model) {return "admin/cs/faq/admin_FAQModify";}

    @GetMapping("/view")
    public String view(Model model) {return "admin/cs/faq/admin_FAQView";}
}
