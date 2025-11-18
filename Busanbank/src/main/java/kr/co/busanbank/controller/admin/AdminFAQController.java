package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminFaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
@Controller
public class AdminFAQController { // 페이징 하기
    private final AdminFaqService adminFaqService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @RequestParam(required = false) String groupCode,
                       @RequestParam(required = false) String faqCategory) {
        log.info("groupCode: {}, faqCategory: {}", groupCode, faqCategory);
        PageResponseDTO pageResponseDTO = adminFaqService.selectAll(pageRequestDTO, groupCode, faqCategory);
        log.info("faq 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);
        model.addAttribute("cate", faqCategory);

        return "admin/cs/faq/admin_FAQList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminFaqService.searchAll(pageRequestDTO);
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
        FaqDTO faqDTO = adminFaqService.findById(faqId);
        log.info("수정 전 데이터: {}", faqDTO);
        model.addAttribute("faqDTO", faqDTO);

        return "admin/cs/faq/admin_FAQModify";
    }

    @PostMapping("/modify")
    public String  modify(FaqDTO faqDTO) {
        log.info("수정 할 데이터 = {}",  faqDTO);
        adminFaqService.modifyFaq(faqDTO);

        return "redirect:/admin/faq/list";
    }


    @GetMapping("/view")
    public String view(int faqId, Model model) {
        log.info("faqId: {}", faqId);
        FaqDTO faqDTO = adminFaqService.findById(faqId);
        log.info("faqDTO={}", faqDTO);
        model.addAttribute("faqDTO", faqDTO);

        return "admin/cs/faq/admin_FAQView";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int faqId) {
        log.info("faqId = {}", faqId);
        adminFaqService.singleDelete(faqId);

        return "redirect:/admin/faq/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminFaqService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
