package kr.co.busanbank.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
@Controller
public class AdminNoticeController {

    @GetMapping("/list")
    public String list(Model model) {return "admin/board/notice/admin_noticeList";}

    @GetMapping("/write")
    public String write(Model model) {return "admin/board/notice/admin_noticeWrite";}

    @GetMapping("/modify")
    public String modify(Model model) {return "admin/board/notice/admin_noticeModify";}

    @GetMapping("/view")
    public String view(Model model) {return "admin/board/notice/admin_noticeView";}
}
