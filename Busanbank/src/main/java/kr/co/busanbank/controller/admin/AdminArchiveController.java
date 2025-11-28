package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.CsPDFDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminArchiveService;
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
@RequestMapping("/admin/archive")
@Controller
public class AdminArchiveController {
    private final AdminArchiveService adminArchiveService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @RequestParam(required = false) String groupCode,
                       @RequestParam(required = false) String archiveCategory) {
        PageResponseDTO pageResponseDTO = adminArchiveService.selectAll(pageRequestDTO, groupCode, archiveCategory);
        log.info("아카이브 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/archive/admin_archiveList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminArchiveService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/archive/admin_archiveList";
    }

    @GetMapping("/write")
    public String write() {return "admin/cs/archive/admin_archiveWrite";}

    @PostMapping("/write")
    public String write(CsPDFDTO  csPDFDTO) throws IOException {
        log.info("csPDFDTO = {}",  csPDFDTO);
        adminArchiveService.insertPDF(csPDFDTO);
        return "redirect:/admin/archive/list";
    }

    @GetMapping("/modify")
    public String modify(int id, Model model) {
        CsPDFDTO csPDFDTO = adminArchiveService.findById(id);
        log.info("수정 전 데이터: {}", csPDFDTO);
        model.addAttribute("csPDFDTO", csPDFDTO);

        return "admin/cs/archive/admin_archiveModify";
    }

    @PostMapping("/modify")
    public String  modify(CsPDFDTO csPDFDTO) throws IOException {
        log.info("수정 할 데이터 = {}",  csPDFDTO);
        adminArchiveService.modifyArchive(csPDFDTO);

        return "redirect:/admin/archive/list";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id = {}", id);
        adminArchiveService.singleDelete(id);

        return "redirect:/admin/archive/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminArchiveService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
