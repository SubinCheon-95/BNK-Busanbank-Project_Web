package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.CsPDFDTO;
import kr.co.busanbank.service.AdminArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/archive")
@Controller
public class AdminArchiveController {
    private final AdminArchiveService adminArchiveService;

    @GetMapping("/write")
    public String write() {return "admin/cs/archive/admin_archiveWrite";}

    @PostMapping("/write")
    public String write(CsPDFDTO  csPDFDTO) throws IOException {
        log.info("csPDFDTO = {}",  csPDFDTO);
        adminArchiveService.insertPDF(csPDFDTO);
        return "";
    }
}
