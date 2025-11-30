/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 은행소개 전용 헤더 GNB 적용
*/

package kr.co.busanbank.controller;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminEventService;
import kr.co.busanbank.service.AdminNoticeService;
import kr.co.busanbank.service.AdminReportService;
import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/company")
public class introduceController {

    private final AdminReportService adminReportService;
    private final AdminNoticeService adminNoticeService;
    private final AdminEventService  adminEventService;

    // 은행소개 GNB용 25.11.29_수빈
    private final CategoryService categoryService;

    @ModelAttribute("coHeaderCategories")
    public Map<String, List<CategoryDTO>> loadCompanyHeaderMenu() {
        Map<String, List<CategoryDTO>> map = new HashMap<>();

        map.put("esg", categoryService.getCategoriesByParentId(25));
        map.put("story", categoryService.getCategoriesByParentId(26));
        map.put("invest", categoryService.getCategoriesByParentId(27));
        map.put("recruit", categoryService.getCategoriesByParentId(28));
        map.put("branch", categoryService.getCategoriesByParentId(29));

        return map;
    }

//    @GetMapping("/company")
//    public String company(Model model) {
//        return  "company/company";
//    }

    @GetMapping("/companyintro")
    public String companyintro(Model model) {

        // parentId = 24 하위 카테고리만 조회
        model.addAttribute("companyCategories",
                categoryService.getCategoriesByParentId(24));

        return  "company/companyintro";
    }

    @GetMapping("/companybankintro")
    public String companybankintro(Model model) {
        return  "company/companybankintro";
    }

    @GetMapping("/companymap")
    public String companymap(Model model) {
        return  "company/companymap";
    }

    @GetMapping("/companystory")
    public String companystory(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO1 = adminReportService.selectAll(pageRequestDTO);
        PageResponseDTO pageResponseDTO2 = adminNoticeService.selectAll(pageRequestDTO);
        PageResponseDTO pageResponseDTO3 = adminEventService.selectAll(pageRequestDTO);

        model.addAttribute("pageResponseDTO1", pageResponseDTO1);
        model.addAttribute("pageResponseDTO2", pageResponseDTO2);
        model.addAttribute("pageResponseDTO3", pageResponseDTO3);

        return  "company/companystory";
    }
    @GetMapping("/companystory/view")
    public String companyStoryView(int id, String boardType,Model model) {
        log.info("id = {}, boardType = {} 테스트용", id, boardType);

        if(boardType.equals("report")) {
            BoardDTO boardDTO = adminReportService.findById(id);
            model.addAttribute("boardDTO", boardDTO);
        }

        else if(boardType.equals("notice")) {
            BoardDTO boardDTO = adminNoticeService.findById(id);
            model.addAttribute("boardDTO", boardDTO);

            boardDTO.setHit(boardDTO.getHit() + 1); //조회수 증가
            adminNoticeService.modifyNoticeHit(boardDTO);
        }

        else if(boardType.equals("event")) {
            BoardDTO boardDTO = adminEventService.findById(id);
            model.addAttribute("boardDTO", boardDTO);
        }

        return  "company/companystoryView";
    }


    @GetMapping("/companyinvest")
    public String companyinvest(Model model) {

        return  "company/companyinvest";
    }

    @GetMapping("/adminproduct")
    public String adminproduct(Model model) {
        return  "company/adminproduct";
    }

    @GetMapping("/quizadmincomplete")
    public String quizadmincomplete(Model model) {
        return  "company/quizadmincomplete";
    }

    @GetMapping("/quizdashboardcomplete")
    public String quizdashboardcomplete(Model model) {
        return  "company/quizdashboardcomplete";
    }

    @GetMapping("/quizresultcomplete")
    public String quizresultcomplete(Model model) {
        return  "company/quizresultcomplete";
    }

    @GetMapping("/quizsolvecomplete")
    public String quizsolvecomplete(Model model) {
        return  "company/quizsolvecomplete";
    }

    @GetMapping("/adminproductcategory")
    public String adminproductcategory(Model model) {
        return  "company/adminproductcategory";
    }

    @GetMapping("/adminsetting")
    public String adminsetting(Model model) {
        return  "company/adminsetting";
    }
}
