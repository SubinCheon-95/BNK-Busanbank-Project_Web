package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.AdminMemberDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@Controller
public class AdminMemberController {
    private final AdminMemberService adminMemberService;

    @GetMapping("/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminMemberService.selectAll(pageRequestDTO);
        log.info("관리자 멤버 관리 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/member/adminMemberList";
    }

    @GetMapping("/detail")
    @ResponseBody
    public AdminMemberDTO detail(@RequestParam int userNo) {
        log.info("userNO 테스트: {}", userNo);

        return adminMemberService.findByUserNo(userNo);
    }
}
