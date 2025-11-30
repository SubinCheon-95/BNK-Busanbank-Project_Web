package kr.co.busanbank.controller;

import kr.co.busanbank.dto.BranchCheckinDTO;
import kr.co.busanbank.dto.BranchDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.BranchCheckinService;
import kr.co.busanbank.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-30
 * 설명: 영업점 체크인 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/branch")
@Controller
public class BranchCheckinController {

    private final BranchCheckinService branchCheckinService;
    private final BranchService branchService;

    /**
     * 영업점 체크인 지도 페이지
     */
    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        log.info("영업점 체크인 페이지");
        return "branch/checkin";
    }

    /**
     * 영업점 목록 조회 API (지도용)
     */
    @GetMapping("/api/branches")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBranchList() {
        log.info("영업점 목록 조회 (지도용)");

        Map<String, Object> response = new HashMap<>();

        try {
            List<BranchDTO> branches = branchService.getAllBranches();

            response.put("success", true);
            response.put("branches", branches);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("영업점 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘 체크인 가능 여부 확인 API
     */
    @GetMapping("/api/canCheckin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> canCheckin(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            boolean canCheckin = branchCheckinService.canCheckinToday(userId);

            response.put("success", true);
            response.put("canCheckin", canCheckin);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("체크인 가능 여부 확인 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "체크인 가능 여부 확인에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 체크인 처리 API
     */
    @PostMapping("/api/checkin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkin(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        log.info("체크인 요청: {}", requestData);

        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            Integer branchId = (Integer) requestData.get("branchId");
            Double userLat = ((Number) requestData.get("latitude")).doubleValue();
            Double userLon = ((Number) requestData.get("longitude")).doubleValue();

            String result = branchCheckinService.processCheckin(userId, branchId, userLat, userLon);

            if ("SUCCESS".equals(result)) {
                response.put("success", true);
                response.put("message", "체크인에 성공했습니다! 100 포인트가 지급되었습니다.");
                response.put("points", 100);
            } else {
                response.put("success", false);
                response.put("message", result);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("체크인 처리 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "체크인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 체크인 히스토리 조회 API
     */
    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCheckinHistory(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            List<BranchCheckinDTO> history = branchCheckinService.getCheckinHistory(userId);

            response.put("success", true);
            response.put("history", history);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("체크인 히스토리 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "체크인 히스토리 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
