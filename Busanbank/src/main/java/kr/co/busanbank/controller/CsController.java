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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/cs/customerSupport")
public class CsController {
    
    private final CategoryPageHelper categoryPageHelper;

    private final CsService csService;
    private final CategoryService categoryService;

    @GetMapping("/faq")
    public String faq(PageRequestDTO pageRequestDTO, Model model) {

        if ("free".equals(pageRequestDTO.getCate())) {
            pageRequestDTO.setCate(null);
        }

        // FAQ 목록 + 페이징
        PageResponseDTO<FaqDTO> pageResponseDTO = csService.getFaqList(pageRequestDTO);

        if ((pageRequestDTO.getCate() == null || pageRequestDTO.getCate().isBlank())
                && pageRequestDTO.getKeyword() != null
                && !pageRequestDTO.getKeyword().isBlank()
                && pageResponseDTO.getDtoList() != null
                && !pageResponseDTO.getDtoList().isEmpty()) {

            // 첫 번째 FAQ의 카테고리 코드
            String firstCate = pageResponseDTO.getDtoList().get(0).getFaqCategory();

            // cate를 세팅하고, 그 cate 기준으로 다시 조회
            pageRequestDTO.setCate(firstCate);
            pageResponseDTO = csService.getFaqList(pageRequestDTO);
        }

        // 카테고리 코드 목록
        List<CodeDetailDTO> faqCategories = csService.getFaqCategories();
        model.addAttribute("pageResponseDTO", pageResponseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("faqCategories", faqCategories);

        categoryPageHelper.setupPage(31, model);

        return "cs/customerSupport/faq";
    }

    @GetMapping("/necessaryDocu")
    public String necessaryDocu(PageRequestDTO pageRequestDTO, Model model) {

        // cate 기본값 정리 (FAQ에서 free → null 쓰던 것처럼)
        if ("free".equals(pageRequestDTO.getCate()) || "all".equals(pageRequestDTO.getCate())) {
            pageRequestDTO.setCate(null);
        }

        // 목록 + 페이징
        PageResponseDTO<DocumentsDTO> pageResponseDTO =
                csService.getDocuments(pageRequestDTO);

        // 카테고리 목록 (수신/신탁/대출/WM/카드/외환/전자금융)
        List<CodeDetailDTO> docCategories = csService.getDocumentCategories();

        model.addAttribute("pageResponseDTO", pageResponseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("docCategories", docCategories);

        categoryPageHelper.setupPage(32, model);

        return "cs/customerSupport/necessaryDocu";
    }

    @GetMapping("/docuView/{docId}")
    public String docuView(@PathVariable int docId,
                           PageRequestDTO pageRequestDTO,
                           Model model) {

        DocumentsDTO doc = csService.getDocument(docId);
        if (doc == null) {
            // 필요하면 커스텀 예외/에러 페이지 처리
            throw new IllegalArgumentException("존재하지 않는 문서입니다.");
        }

        model.addAttribute("doc", doc);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        categoryPageHelper.setupPage(32, model);

        return "cs/customerSupport/docuView";
    }


    @GetMapping("/login/talkCounsel")
    public String talkCounsel(Model model) {

        categoryPageHelper.setupPage(33, model);

        return "cs/customerSupport/login/talkCounsel";
    }
    
}

