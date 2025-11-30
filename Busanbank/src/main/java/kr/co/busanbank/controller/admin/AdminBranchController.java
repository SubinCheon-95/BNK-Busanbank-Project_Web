package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.BranchDTO;
import kr.co.busanbank.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-30
 * 설명: 영업점 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/branch")
@Controller
public class AdminBranchController {

    private final BranchService branchService;

    /**
     * 영업점 관리 페이지
     */
    @GetMapping("")
    public String branch(Model model) {
        log.info("Admin Branch Management Page");
        return "admin/adminbranch";
    }

    /**
     * 영업점 목록 조회 API
     */
    @GetMapping("/branches")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBranchList() {
        log.info("영업점 목록 조회");

        Map<String, Object> response = new HashMap<>();

        try {
            List<BranchDTO> branchList = branchService.getAllBranches();

            response.put("success", true);
            response.put("data", branchList);
            response.put("totalCount", branchList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("영업점 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 영업점 상세 조회 API
     */
    @GetMapping("/branches/{branchId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBranch(@PathVariable Integer branchId) {
        log.info("영업점 상세 조회 - branchId: {}", branchId);

        Map<String, Object> response = new HashMap<>();

        try {
            BranchDTO branch = branchService.getBranchById(branchId);

            if (branch != null) {
                response.put("success", true);
                response.put("data", branch);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "영업점을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("영업점 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 영업점 등록 API
     */
    @PostMapping("/branches")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBranch(@RequestBody BranchDTO branch) {
        log.info("영업점 등록: {}", branch);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = branchService.createBranch(branch);

            if (result) {
                response.put("success", true);
                response.put("message", "영업점이 성공적으로 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "영업점 등록에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("영업점 등록 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 등록 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 영업점 수정 API
     */
    @PutMapping("/branches/{branchId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBranch(
            @PathVariable Integer branchId,
            @RequestBody BranchDTO branch) {
        log.info("영업점 수정 - branchId: {}, data: {}", branchId, branch);

        Map<String, Object> response = new HashMap<>();

        try {
            branch.setBranchId(branchId);
            boolean result = branchService.updateBranch(branch);

            if (result) {
                response.put("success", true);
                response.put("message", "영업점이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "영업점 수정에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("영업점 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 수정 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 영업점 삭제 API
     */
    @DeleteMapping("/branches/{branchId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBranch(@PathVariable Integer branchId) {
        log.info("영업점 삭제 - branchId: {}", branchId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = branchService.deleteBranch(branchId);

            if (result) {
                response.put("success", true);
                response.put("message", "영업점이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "영업점 삭제에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("영업점 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "영업점 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
