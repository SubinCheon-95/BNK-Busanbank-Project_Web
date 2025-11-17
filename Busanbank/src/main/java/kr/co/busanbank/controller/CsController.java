package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CsController {

    private final CsService csService;

    @GetMapping("/cs/customerSupport/faq")
    public String faq(PageRequestDTO pageRequestDTO, Model model) {

        if ("free".equals(pageRequestDTO.getCate())) {
            pageRequestDTO.setCate(null);
        }

        // FAQ 목록 + 페이징
        PageResponseDTO<FaqDTO> pageResponseDTO = csService.getFaqList(pageRequestDTO);

        // 카테고리 코드 목록
        List<CodeDetailDTO> faqCategories = csService.getFaqCategories();

        model.addAttribute("pageResponseDTO", pageResponseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("faqCategories", faqCategories);

        return "cs/customerSupport/faq";
    }

    @GetMapping("/cs/customerSupport/necessaryDocu")
    public String necessaryDocu() {
        return "cs/customerSupport/necessaryDocu";
    }

    @GetMapping("/cs/customerSupport/docuView")
    public String docuView() {
        return "cs/customerSupport/docuView";
    }

    @GetMapping("/cs/customerSupport/onlineCounsel")
    public String onlineCounsel() {
        return "cs/customerSupport/onlineCounsel";
    }

    @GetMapping("/cs/customerSupport/talkCounsel")
    public String talkCounsel() {
        return "cs/customerSupport/talkCounsel";
    }

}