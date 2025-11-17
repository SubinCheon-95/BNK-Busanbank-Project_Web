package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminFaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
@Controller
public class AdminFAQController {
    private final AdminFaqService adminFaqService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminFaqService.selectAll(pageRequestDTO);
        log.info("faq 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/faq/admin_FAQList";
    }

    @GetMapping("/write")
    public String write(Model model) {return "admin/cs/faq/admin_FAQWrite";}

    @PostMapping("/write")
    public String write(FaqDTO faqDTO) {
        log.info("faqDTO = {}",  faqDTO);
        adminFaqService.insertFaq(faqDTO);

        return "redirect:/admin/faq/list";
    }

    @GetMapping("/modify")
    public String modify(int faqId, Model model) {

        return "admin/cs/faq/admin_FAQModify";
    }

    @GetMapping("/view")
    public String view(Model model) {return "admin/cs/faq/admin_FAQView";}

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int faqId) {
        log.info("faqId = {}", faqId);
        adminFaqService.singleDelete(faqId);

        return "admin/cs/faq/admin_FAQList";
    }
}
