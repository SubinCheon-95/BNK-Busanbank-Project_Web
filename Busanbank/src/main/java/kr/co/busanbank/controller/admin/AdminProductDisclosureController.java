package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.DisclosureDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminDisclosureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/disclosure")
@Controller
public class AdminProductDisclosureController { //수정 기능만 넣으면 끝
    private final AdminDisclosureService adminDisclosureService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @RequestParam(required = false) String groupCode,
                       @RequestParam(required = false) String disclosureCategory) {
        PageResponseDTO pageResponseDTO = adminDisclosureService.selectAll(pageRequestDTO, groupCode, disclosureCategory);
        log.info("상품공시실 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/disclosure/admin_disclosureList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminDisclosureService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/cs/disclosure/admin_disclosureList";
    }
    @GetMapping("/write")
    public String write() { return "admin/cs/disclosure/admin_disclosureWrite"; }

    @PostMapping("/write")
    public String write(DisclosureDTO disclosureDTO) throws IOException {
        log.info("disclosureDTO = {}", disclosureDTO);

        // 💡 여러 파일을 확인하는 핵심 로직
        List<MultipartFile> files = disclosureDTO.getUploadFile();

        if (files != null && !files.isEmpty()) {
            log.info("총 업로드된 파일 개수: {}", files.size());

            for (MultipartFile file : files) {
                // 각 파일의 정보를 확인
                log.info("파일명: {}", file.getOriginalFilename());
                log.info("파일 크기: {} bytes", file.getSize());
                log.info("파일 타입: {}", file.getContentType());
            }
        } else {
            log.warn("업로드된 파일이 없습니다.");
        }

        adminDisclosureService.insertPDF(disclosureDTO);
        return "redirect:/admin/disclosure/list";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id = {}", id);
        adminDisclosureService.singleDelete(id);

        return "redirect:/admin/disclosure/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminDisclosureService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
