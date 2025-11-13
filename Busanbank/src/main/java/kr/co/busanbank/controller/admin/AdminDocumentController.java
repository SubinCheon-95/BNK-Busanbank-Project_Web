package kr.co.busanbank.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/docu")
@Controller
public class AdminDocumentController {

    @GetMapping("/list")
    public String list(Model model) {return "admin/cs/document/admin_documentList";}

    @GetMapping("/write")
    public String write(Model model) {return "admin/cs/document/admin_documentWrite";}

    @GetMapping("/modify")
    public String modify(Model model) {return "admin/cs/document/admin_documentModify";}

    @GetMapping("/view")
    public String view(Model model) {return "admin/cs/document/admin_documentView";}
}
