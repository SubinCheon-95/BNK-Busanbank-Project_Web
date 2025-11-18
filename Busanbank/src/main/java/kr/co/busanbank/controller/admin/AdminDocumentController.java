package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.DocumentsDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/docu")
@Controller
public class AdminDocumentController {
    private final AdminDocService  adminDocService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @RequestParam(required = false) String groupCode,
                       @RequestParam(required = false) String docCategory) {
        log.info("groupCode: {}, docCategory: {}", groupCode, docCategory);
        PageResponseDTO pageResponseDTO = adminDocService.selectAll(pageRequestDTO, groupCode, docCategory);
        log.info("doc 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);
        model.addAttribute("cate", docCategory);

        return "admin/cs/document/admin_documentList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminDocService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/document/admin_documentList";
    }

    @GetMapping("/write")
    public String write(Model model) {return "admin/cs/document/admin_documentWrite";}

    @PostMapping("/write")
    public String write(DocumentsDTO documentsDTO) {
        log.info("documentsDTO = {}",  documentsDTO);
        adminDocService.insertDoc(documentsDTO);

        return "redirect:/admin/docu/list";
    }

    @GetMapping("/modify")
    public String modify(int docId, Model model) {
        DocumentsDTO documentsDTO = adminDocService.findById(docId);
        log.info("수정 전 데이터: {}", documentsDTO);
        model.addAttribute("documentsDTO", documentsDTO);

        return "admin/cs/document/admin_documentModify";
    }

    @PostMapping("/modify")
    public String  modify(DocumentsDTO  documentsDTO) {
        log.info("수정 할 데이터 = {}",  documentsDTO);
        adminDocService.modifyDoc(documentsDTO);

        return "redirect:/admin/docu/list";
    }

    @GetMapping("/view")
    public String view(int docId, Model model) {
        log.info("docId: {}", docId);
        DocumentsDTO documentsDTO = adminDocService.findById(docId);
        log.info("documentsDTO={}", documentsDTO);
        model.addAttribute("documentsDTO", documentsDTO);

        return "admin/cs/document/admin_documentView";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int docId) {
        log.info("docId: {}", docId);
        adminDocService.singleDelete(docId);

        return "redirect:/admin/docu/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminDocService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
