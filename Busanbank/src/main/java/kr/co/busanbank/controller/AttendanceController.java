package kr.co.busanbank.controller;

import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 출석체크 컨트롤러
 */
@Controller
@RequestMapping("/member/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 출석체크 페이지
     */
    @GetMapping
    public String attendancePage(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        int userId = userDetails.getUsersDTO().getUserNo();

        // 오늘 출석 여부
        boolean isAttendedToday = attendanceService.isAttendedToday(userId);
        model.addAttribute("isAttendedToday", isAttendedToday);

        // 현재 연속 출석 일수
        int consecutiveDays = attendanceService.getCurrentConsecutiveDays(userId);
        model.addAttribute("consecutiveDays", consecutiveDays);

        // 출석 보상 정보
        model.addAttribute("rewards", attendanceService.getAllRewards());

        return "member/attendance/attendance";
    }

    /**
     * 출석체크 처리 API
     */
    @PostMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAttendance(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            result = attendanceService.checkAttendance(userId);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석체크 처리 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 이력 조회 API
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAttendanceHistory(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            Map<String, Object> historyData = attendanceService.getAttendanceHistory(userId, page, size);

            result.put("success", true);
            result.put("data", historyData);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 이력 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 상태 조회 API
     */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAttendanceStatus(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();

            boolean isAttendedToday = attendanceService.isAttendedToday(userId);
            int consecutiveDays = attendanceService.getCurrentConsecutiveDays(userId);

            result.put("success", true);
            result.put("isAttendedToday", isAttendedToday);
            result.put("consecutiveDays", consecutiveDays);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 상태 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
