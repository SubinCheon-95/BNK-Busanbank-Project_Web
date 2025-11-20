package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
@Controller
public class AdminNoticeController {
    private final AdminNoticeService adminNoticeService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminNoticeService.selectAll(pageRequestDTO);
        log.info("공지사항 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/notice/admin_noticeList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminNoticeService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/notice/admin_noticeList";
    }

    @GetMapping("/write")
    public String write(Model model) {return "admin/board/notice/admin_noticeWrite";}

    @PostMapping("/write")
    public String write(BoardDTO boardDTO) {
        adminNoticeService.insertNotice(boardDTO);

        return "redirect:/admin/notice/list";
    }

    @GetMapping("/modify")
    public String modify(int id, Model model) {
        BoardDTO boardDTO = adminNoticeService.findById(id);
        log.info("수정 전 데이터: {}", boardDTO);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/notice/admin_noticeModify";
    }

    @PostMapping("/modify")
    public String  modify(BoardDTO boardDTO) {
        log.info("수정 할 데이터 = {}",  boardDTO);
        adminNoticeService.modifyNotice(boardDTO);

        return "redirect:/admin/notice/list";
    }

    @GetMapping("/view")
    public String view(int id, Model model) {
        BoardDTO boardDTO = adminNoticeService.findById(id);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/notice/admin_noticeView";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id: {}", id);
        adminNoticeService.singleDelete(id);

        return "redirect:/admin/notice/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        adminNoticeService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
