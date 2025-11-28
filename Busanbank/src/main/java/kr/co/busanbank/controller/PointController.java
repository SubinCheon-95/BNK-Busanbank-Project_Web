package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UserPointDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.PointService;
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
 * 설명: 포인트 컨트롤러
 */
@Controller
@RequestMapping("/member/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 내 포인트 페이지
     */
    @GetMapping
    public String pointPage(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        int userId = userDetails.getUsersDTO().getUserNo();
        UserPointDTO userPoint = pointService.getUserPoint(userId);

        model.addAttribute("userPoint", userPoint);
        return "member/point/mypoint";
    }

    /**
     * 포인트 정보 조회 API
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPointInfo(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            UserPointDTO userPoint = pointService.getUserPoint(userId);

            result.put("success", true);
            result.put("data", userPoint);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "포인트 정보 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 포인트 히스토리 조회 API
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPointHistory(
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
            Map<String, Object> historyData = pointService.getPointHistory(userId, page, size);

            result.put("success", true);
            result.put("data", historyData);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "포인트 히스토리 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 월별 획득 포인트 조회 API
     */
    @GetMapping("/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyPoints(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            int monthlyPoints = pointService.getMonthlyEarnedPoints(userId, year, month);

            result.put("success", true);
            result.put("monthlyPoints", monthlyPoints);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "월별 포인트 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
