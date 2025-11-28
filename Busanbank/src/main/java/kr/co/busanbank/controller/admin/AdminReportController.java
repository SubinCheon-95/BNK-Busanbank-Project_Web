package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/report")
@Controller
public class AdminReportController {
    private final AdminReportService  adminReportService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminReportService.selectAll(pageRequestDTO);
        log.info("보도자료 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/report/admin_reportList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminReportService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/report/admin_reportList";
    }

    @GetMapping("/write")
    public String write(Model model) {return "admin/board/report/admin_reportWrite";}

    @PostMapping("/write")
    public String write(BoardDTO boardDTO, RedirectAttributes redirectAttributes) throws IOException {
        try {
            adminReportService.insertReport(boardDTO);
            redirectAttributes.addFlashAttribute("message", "보도자료가 등록되었습니다.");
            return "redirect:/admin/report/list";
        } catch (IOException e) {
            log.error("보도자료 등록 실패", e);
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
            return "redirect:/admin/report/write";
        }
    }

    @GetMapping("/modify")
    public String modify(int id, Model model) {
        BoardDTO boardDTO = adminReportService.findById(id);
        log.info("수정 전 데이터: {}", boardDTO);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/report/admin_reportModify";
    }

    @PostMapping("/modify")
    public String  modify(BoardDTO boardDTO, RedirectAttributes redirectAttributes) throws IOException {
        try {
            log.info("수정 할 데이터 = {}", boardDTO);
            adminReportService.modifyReport(boardDTO);
            redirectAttributes.addFlashAttribute("message", "보도자료가 수정되었습니다.");
            return "redirect:/admin/report/list";
        }  catch (IOException e) {
            log.error("보도자료 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
            return "redirect:/admin/report/modify?id=" + boardDTO.getId();
        }
    }

    @GetMapping("/view")
    public String view(int id, Model model) {
        log.info("id: {}", id);
        BoardDTO boardDTO = adminReportService.findById(id);
        log.info("boardDTO={}", boardDTO);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/report/admin_reportView";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id: {}", id);
        adminReportService.singleDelete(id);

        return "redirect:/admin/report/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminReportService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
