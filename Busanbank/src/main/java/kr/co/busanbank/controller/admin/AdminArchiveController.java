package kr.co.busanbank.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/archive")
@Controller
public class AdminArchiveController {

    @GetMapping("/write")
    public String write() {return "admin/cs/archive/admin_archiveWrite";}
}
