package kr.co.busanbank.controller;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminEventService;
import kr.co.busanbank.service.AdminNoticeService;
import kr.co.busanbank.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/company")
public class introduceController {
    private final AdminReportService adminReportService;
    private final AdminNoticeService adminNoticeService;
    private final AdminEventService  adminEventService;

    @GetMapping("/company")
    public String company(Model model) {
        return  "company/company";
    }

    @GetMapping("/companyintro")
    public String companyintro(Model model) {
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
