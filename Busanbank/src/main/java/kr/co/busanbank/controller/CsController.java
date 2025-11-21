package kr.co.busanbank.controller;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.service.CategoryService;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CsController {

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

        setupPage(31, model);

        return "cs/customerSupport/faq";
    }

    @GetMapping("/cs/customerSupport/necessaryDocu")
    public String necessaryDocu(Model model) {

        setupPage(32, model);

        return "cs/customerSupport/necessaryDocu";
    }

    @GetMapping("/cs/customerSupport/docuView")
    public String docuView() {

        return "cs/customerSupport/docuView";
    }

    @GetMapping("/cs/customerSupport/onlineCounsel")
    public String onlineCounsel(Model model) {

        setupPage(34, model);

        return "cs/customerSupport/onlineCounsel";
    }

    @GetMapping("/cs/customerSupport/talkCounsel")
    public String talkCounsel(Model model) {

        setupPage(33, model);

        return "cs/customerSupport/talkCounsel";
    }

    private void setupPage(int categoryId, Model model) {
        CategoryDTO current = categoryService.getCategoryById(categoryId);
        List<CategoryDTO> breadcrumb = categoryService.getBreadcrumb(categoryId);
        CategoryDTO root = breadcrumb.get(0);

        List<CategoryDTO> depth2List = categoryService.getChildren(root.getCategoryId());

        Map<Integer, List<CategoryDTO>> depth3Map = new HashMap<>();
        for (CategoryDTO depth2 : depth2List) {
            List<CategoryDTO> children = categoryService.getChildren(depth2.getCategoryId());
            depth3Map.put(depth2.getCategoryId(), children);
        }

        Integer activeDepth2Id;
        if (breadcrumb.size() >= 2) {
            activeDepth2Id = breadcrumb.get(1).getCategoryId();
        } else {
            activeDepth2Id = current.getParentId() != null
                    ? current.getParentId()
                    : current.getCategoryId();
        }

        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("pageTitle", current.getCategoryName());
        model.addAttribute("rootCategory", root);
        model.addAttribute("depth2Menu", depth2List);
        model.addAttribute("depth3Map", depth3Map);
        model.addAttribute("activeDepth2Id", activeDepth2Id);
        model.addAttribute("currentCategoryId", categoryId);
    }
}

