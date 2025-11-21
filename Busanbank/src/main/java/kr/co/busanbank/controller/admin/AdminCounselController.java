package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/*
    이름: 윤종인
    작성일: 2025-11-21
    설명: 이메일 상담 컨트롤러
 */

@Slf4j
@RequiredArgsConstructor //write 할때 보이게 하기
@RequestMapping("/admin/counsel")
@Controller
public class AdminCounselController {
    private final AdminEmailService adminEmailService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminEmailService.selectAll(pageRequestDTO);
        log.info("이메일 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/emailCounsel/admin_emailCounselList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminEmailService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/emailCounsel/admin_emailCounselList";
    }

    @GetMapping("/write")
    public String write(int ecounselId, Model model) {
        EmailCounselDTO emailCounselDTO = adminEmailService.findById(ecounselId);
        log.info("수정 전 데이터: {}", emailCounselDTO);
        model.addAttribute("emailCounselDTO", emailCounselDTO);

        return "admin/cs/emailCounsel/admin_emailCounselWrite";
    }

    @PostMapping("/write")
    public String write(EmailCounselDTO emailCounselDTO) {
        adminEmailService.insertEmail(emailCounselDTO);

        return "redirect:/admin/counsel/list";
    }

    @GetMapping("/modify")
    public String modify(int ecounselId, Model model) {
        EmailCounselDTO emailCounselDTO = adminEmailService.findById(ecounselId);
        model.addAttribute("emailCounselDTO", emailCounselDTO);

        return "admin/cs/emailCounsel/admin_emailCounselModify";
    }

    @PostMapping("/modify")
    public String  modify(EmailCounselDTO emailCounselDTO) {
        adminEmailService.modifyEmail(emailCounselDTO);

        return "redirect:/admin/counsel/list";
    }

    @GetMapping("/view")
    public String view(int ecounselId, Model model) {
        EmailCounselDTO emailCounselDTO = adminEmailService.findById(ecounselId);
        model.addAttribute("emailCounselDTO", emailCounselDTO);

        return "admin/cs/emailCounsel/admin_emailCounselView";
    }
}