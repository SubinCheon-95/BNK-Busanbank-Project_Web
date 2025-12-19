package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.NotificationDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping("/notification/list")
    public String list(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminNotificationService.selectAll(pageRequestDTO);
        log.info("푸시알림 리스트: {}", pageResponseDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/app/notification/admin_notificationList";
    }

    @GetMapping("/notification/list/search")
    public String searchList(Model model, PageRequestDTO pageRequestDTO) {
        PageResponseDTO pageResponseDTO = adminNotificationService.searchAll(pageRequestDTO);
        model.addAttribute("pageResponseDTO", pageResponseDTO);

        return "admin/app/notification/admin_notificationList";
    }

    @GetMapping("/notification/write")
    public String write(Model model) {return "admin/app/notification/admin_notificationWrite";}

    @PostMapping("/notification/write")
    @ResponseBody
    public Map<String, Object> write(@RequestBody NotificationDTO notificationDTO) {
        log.info("푸시 알림 데이터 = {}", notificationDTO);

        if ("Y".equals(notificationDTO.getAutoBtn())) {
            if (notificationDTO.getCronExpr() == null || notificationDTO.getCronExpr().isBlank()) {
                notificationDTO.setCronExpr("0 * * * * ?");
            }
            adminNotificationService.insertAuto(notificationDTO);
        } else {
            adminNotificationService.insertPush(notificationDTO);
        }

        return Map.of(
                "success", true,
                "message", "푸시 전송 요청 완료"
        );
    }

    @GetMapping("/notification/delete")
    public String singleDelete(@RequestParam int id) {
        log.info("id: {}", id);
        adminNotificationService.singleDelete(id);

        return "redirect:/admin/notification/list";
    }

    @DeleteMapping("/notification/list")
    @ResponseBody
    public ResponseEntity<Void> delete(@RequestBody List<Long> idList) {
        log.info("idList: {}", idList);
        adminNotificationService.delete(idList);

        return ResponseEntity.ok().build();
    }
}