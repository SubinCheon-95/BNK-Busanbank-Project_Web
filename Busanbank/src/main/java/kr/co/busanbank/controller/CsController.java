package kr.co.busanbank.controller;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.helper.CategoryPageHelper;
import kr.co.busanbank.service.CategoryService;
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
    
    private final CategoryPageHelper categoryPageHelper;

    private final CsService csService;
    private final CategoryService categoryService;

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

        categoryPageHelper.setupPage(31, model);

        return "cs/customerSupport/faq";
    }

    @GetMapping("/cs/customerSupport/necessaryDocu")
    public String necessaryDocu(Model model) {

        categoryPageHelper.setupPage(32, model);

        return "cs/customerSupport/necessaryDocu";
    }

    @GetMapping("/cs/customerSupport/docuView")
    public String docuView(Model model) {

        categoryPageHelper.setupPage(32, model);

        return "cs/customerSupport/docuView";
    }


    @GetMapping("/cs/customerSupport/login/talkCounsel")
    public String talkCounsel(Model model) {

        categoryPageHelper.setupPage(33, model);

        return "cs/customerSupport/login/talkCounsel";
    }
    
}

