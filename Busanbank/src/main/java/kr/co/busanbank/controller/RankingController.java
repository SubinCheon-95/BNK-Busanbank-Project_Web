package kr.co.busanbank.controller;

import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.RankingService;
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
 * 설명: 랭킹 컨트롤러
 */
@Controller
@RequestMapping("/member/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 페이지
     */
    @GetMapping
    public String rankingPage(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        int userId = userDetails.getUsersDTO().getUserNo();
        Map<String, Object> rankingData = rankingService.getRankingData(userId);

        model.addAllAttributes(rankingData);
        return "member/ranking/ranking";
    }

    /**
     * 전체 TOP 10 조회 API
     */
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRankingAll(
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("data", rankingService.getTopRankingAll(limit));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "랭킹 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 월별 TOP 10 조회 API
     */
    @GetMapping("/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRankingMonthly(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("data", rankingService.getTopRankingMonthly(year, month, limit));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "월별 랭킹 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 내 순위 조회 API
     */
    @GetMapping("/my-rank")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyRank(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();

            Integer allRank = rankingService.getUserRankAll(userId);
            Integer monthlyRank = rankingService.getUserRankCurrentMonth(userId);

            result.put("success", true);
            result.put("allRank", allRank);
            result.put("monthlyRank", monthlyRank);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "내 순위 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 랭킹 종합 데이터 API
     */
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRankingData(@AuthenticationPrincipal MyUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userDetails == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            int userId = userDetails.getUsersDTO().getUserNo();
            Map<String, Object> rankingData = rankingService.getRankingData(userId);

            result.put("success", true);
            result.put("data", rankingData);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "랭킹 데이터 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
