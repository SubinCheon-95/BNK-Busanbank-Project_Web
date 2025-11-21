package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/event")
@Controller
public class AdminEventController {
    private final AdminEventService adminEventService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminEventService.selectAll(pageRequestDTO);
        log.info("이벤트 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/event/admin_eventList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminEventService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/event/admin_eventList";
    }

    @GetMapping("/write")
    public String write(Model model) {return "admin/board/event/admin_eventWrite";}

    @PostMapping("/write")
    public String write(BoardDTO boardDTO) throws IOException  {
        adminEventService.insertEvent(boardDTO);

        return "redirect:/admin/event/list";
    }

    @GetMapping("/modify")
    public String modify(int id, Model model) {
        BoardDTO boardDTO = adminEventService.findById(id);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/event/admin_eventModify";
    }

    @PostMapping("/modify")
    public String  modify(BoardDTO boardDTO) throws IOException {
        adminEventService.modifyEvent(boardDTO);

        return "redirect:/admin/event/list";
    }

    @GetMapping("/view")
    public String view(int id, Model model) {
        BoardDTO boardDTO = adminEventService.findById(id);
        model.addAttribute("boardDTO", boardDTO);

        return "admin/board/event/admin_eventView";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        adminEventService.singleDelete(id);

        return "redirect:/admin/event/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        adminEventService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
