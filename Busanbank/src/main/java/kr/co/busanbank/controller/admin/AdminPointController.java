package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.AttendanceRewardDTO;
import kr.co.busanbank.dto.LevelSettingDTO;
import kr.co.busanbank.service.AttendanceService;
import kr.co.busanbank.service.LevelService;
import kr.co.busanbank.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 관리자 포인트 관리 컨트롤러
 */
@Controller
@RequestMapping("/admin/point")
@RequiredArgsConstructor
public class AdminPointController {

    private final PointService pointService;
    private final LevelService levelService;
    private final AttendanceService attendanceService;

    /**
     * 레벨 설정 페이지
     */
    @GetMapping("/level")
    public String levelPage(Model model) {
        model.addAttribute("levels", levelService.getAllLevels());
        return "admin/point/level";
    }

    /**
     * 레벨 설정 목록 조회 API
     */
    @GetMapping("/levels")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLevels() {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("data", levelService.getAllLevels());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "레벨 목록 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 레벨 설정 추가 API
     */
    @PostMapping("/levels")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addLevel(@RequestBody LevelSettingDTO levelDTO) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = levelService.addLevel(levelDTO);

            if (success) {
                result.put("success", true);
                result.put("message", "레벨이 추가되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "레벨 추가에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "레벨 추가 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 레벨 설정 수정 API
     */
    @PutMapping("/levels/{levelId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateLevel(
            @PathVariable int levelId,
            @RequestBody LevelSettingDTO levelDTO) {

        Map<String, Object> result = new HashMap<>();

        try {
            levelDTO.setLevelId(levelId);
            boolean success = levelService.updateLevel(levelDTO);

            if (success) {
                result.put("success", true);
                result.put("message", "레벨이 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "레벨 수정에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "레벨 수정 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 레벨 설정 삭제 API
     */
    @DeleteMapping("/levels/{levelId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteLevel(@PathVariable int levelId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = levelService.deleteLevel(levelId);

            if (success) {
                result.put("success", true);
                result.put("message", "레벨이 삭제되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "레벨 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "레벨 삭제 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 보상 설정 페이지
     */
    @GetMapping("/attendance-reward")
    public String attendanceRewardPage(Model model) {
        model.addAttribute("rewards", attendanceService.getAllRewards());
        return "admin/point/attendance-reward";
    }

    /**
     * 출석 보상 설정 목록 조회 API
     */
    @GetMapping("/attendance-rewards")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAttendanceRewards() {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("data", attendanceService.getAllRewards());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 보상 목록 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 보상 설정 추가 API
     */
    @PostMapping("/attendance-rewards")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addAttendanceReward(@RequestBody AttendanceRewardDTO rewardDTO) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = attendanceService.addReward(rewardDTO);

            if (success) {
                result.put("success", true);
                result.put("message", "출석 보상이 추가되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "출석 보상 추가에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 보상 추가 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 보상 설정 수정 API
     */
    @PutMapping("/attendance-rewards/{rewardId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAttendanceReward(
            @PathVariable int rewardId,
            @RequestBody AttendanceRewardDTO rewardDTO) {

        Map<String, Object> result = new HashMap<>();

        try {
            rewardDTO.setRewardId(rewardId);
            boolean success = attendanceService.updateReward(rewardDTO);

            if (success) {
                result.put("success", true);
                result.put("message", "출석 보상이 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "출석 보상 수정에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 보상 수정 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 출석 보상 설정 삭제 API
     */
    @DeleteMapping("/attendance-rewards/{rewardId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttendanceReward(@PathVariable int rewardId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = attendanceService.deleteReward(rewardId);

            if (success) {
                result.put("success", true);
                result.put("message", "출석 보상이 삭제되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "출석 보상 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석 보상 삭제 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 포인트 수동 지급 API (관리자용)
     */
    @PostMapping("/give-points")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> givePoints(
            @RequestParam int userId,
            @RequestParam int amount,
            @RequestParam String description) {

        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = pointService.earnPoints(userId, amount, description);

            if (success) {
                result.put("success", true);
                result.put("message", "포인트가 지급되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "포인트 지급에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "포인트 지급 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
