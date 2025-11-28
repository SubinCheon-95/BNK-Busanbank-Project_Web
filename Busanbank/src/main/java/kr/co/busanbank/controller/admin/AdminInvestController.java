package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.InvestDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminInvestService;
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
@RequestMapping("/admin/invest")
@Controller
public class AdminInvestController {
    private final AdminInvestService adminInvestService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @RequestParam(required = false) String investType) {
        PageResponseDTO pageResponseDTO = adminInvestService.selectAll(pageRequestDTO, investType);
        log.info("투자자 정보 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/invest/admin_investList";
    }

    @GetMapping("/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminInvestService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/board/invest/admin_investList";
    }

    @GetMapping("/write")
    public String write() {return "admin/board/invest/admin_investWrite";}

    @PostMapping("/write")
    public String write(InvestDTO  investDTO) throws IOException  {
        log.info("investDTO = {}",  investDTO);
        adminInvestService.insertPDF(investDTO);
        return "redirect:/admin/invest/list";
    }

    @GetMapping("/modify")
    public String modify(int id, Model model) {
        InvestDTO investDTO = adminInvestService.findById(id);
        log.info("수정 전 데이터: {}", investDTO);
        model.addAttribute("investDTO", investDTO);

        return "admin/board/invest/admin_investModify";
    }

    @PostMapping("/modify")
    public String  modify(InvestDTO  investDTO) throws IOException {
        log.info("수정 할 데이터 = {}",  investDTO);
        adminInvestService.modifyInvest(investDTO);

        return "redirect:/admin/invest/list";
    }

    @GetMapping("/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id = {}", id);
        adminInvestService.singleDelete(id);

        return "redirect:/admin/invest/list";
    }

    @DeleteMapping("/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList = " + idList);
        adminInvestService.delete(idList);

        return ResponseEntity.ok().build();
    }
}
