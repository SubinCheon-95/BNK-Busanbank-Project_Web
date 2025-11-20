package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.dto.SiteSettingDTO;
import kr.co.busanbank.service.AdminService;
import kr.co.busanbank.service.SecuritySettingService;
import kr.co.busanbank.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 수정일: 2025-11-19 (사이트 설정 기능 추가)
 * 수정일: 2025-11-20 (보안 설정 기능 추가)
 * 설명: 환경설정 및 관리자 계정 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/setting")
@Controller
public class AdminSettingController {

    private final AdminService adminService;
    private final SiteSettingService siteSettingService;
    private final SecuritySettingService securitySettingService;

    /**
     * 환경설정 페이지
     */
    @GetMapping("")
    public String setting(@RequestParam(value = "tab", required = false) String tab, Model model) {
        log.info("Admin Setting Page - tab: {}", tab);

        if (tab != null) {
            model.addAttribute("tab", tab);
        }

        return "admin/adminsetting";
    }

    /**
     * 관리자 목록 조회 API
     */
    @GetMapping("/admins")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAdminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword
    ) {
        log.info("관리자 목록 조회 - page: {}, size: {}, keyword: {}", page, size, searchKeyword);

        Map<String, Object> response = new HashMap<>();

        try {
            List<AdminDTO> adminList = adminService.getAdminList(page, size, searchKeyword);
            int totalCount = adminService.getTotalCount(searchKeyword);
            int totalPages = (int) Math.ceil((double) totalCount / size);

            response.put("success", true);
            response.put("data", adminList);
            response.put("totalCount", totalCount);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("관리자 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "관리자 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 관리자 상세 조회 API
     */
    @GetMapping("/admins/{adminId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAdmin(@PathVariable int adminId) {
        log.info("관리자 상세 조회 - adminId: {}", adminId);

        Map<String, Object> response = new HashMap<>();

        try {
            AdminDTO admin = adminService.getAdminById(adminId);

            if (admin != null) {
                response.put("success", true);
                response.put("data", admin);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "관리자를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("관리자 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "관리자 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 관리자 추가 API
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PostMapping("/admins")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody AdminDTO adminDTO) {
        log.info("관리자 추가 - loginId: {}", adminDTO.getLoginId());

        Map<String, Object> response = new HashMap<>();

        try {
            // LoginId 중복 체크
            if (adminService.isLoginIdDuplicate(adminDTO.getLoginId())) {
                response.put("success", false);
                response.put("message", "이미 존재하는 로그인 ID입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = adminService.createAdmin(adminDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "관리자가 성공적으로 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "관리자 추가에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반 등 유효성 검증 실패
            log.warn("관리자 추가 유효성 검증 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("관리자 추가 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "관리자 추가에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 관리자 수정 API
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PutMapping("/admins/{adminId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAdmin(
            @PathVariable int adminId,
            @RequestBody AdminDTO adminDTO
    ) {
        log.info("관리자 수정 - adminId: {}", adminId);

        Map<String, Object> response = new HashMap<>();

        try {
            adminDTO.setAdminId(adminId);
            boolean result = adminService.updateAdmin(adminDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "관리자가 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "관리자 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반 등 유효성 검증 실패
            log.warn("관리자 수정 유효성 검증 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("관리자 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "관리자 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 관리자 삭제 API
     */
    @DeleteMapping("/admins/{adminId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable int adminId) {
        log.info("관리자 삭제 - adminId: {}", adminId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = adminService.deleteAdmin(adminId);

            if (result) {
                response.put("success", true);
                response.put("message", "관리자가 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "관리자 삭제에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("관리자 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "관리자 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * LoginId 중복 체크 API
     */
    @GetMapping("/admins/check-loginid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLoginId(@RequestParam String loginId) {
        log.info("LoginId 중복 체크 - loginId: {}", loginId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = adminService.isLoginIdDuplicate(loginId);
            response.put("success", true);
            response.put("isDuplicate", isDuplicate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("LoginId 중복 체크 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "중복 체크에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ========== 사이트 기본설정 관련 API (작성자: 진원, 2025-11-19) ==========

    /**
     * 사이트 기본설정 조회 API
     */
    @GetMapping("/site")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSiteSettings() {
        log.info("사이트 기본설정 조회");

        Map<String, Object> response = new HashMap<>();

        try {
            List<SiteSettingDTO> settings = siteSettingService.getAllSettings();

            response.put("success", true);
            response.put("data", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사이트 설정 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "사이트 설정 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사이트 기본설정 수정 API
     */
    @PutMapping("/site")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSiteSetting(
            @RequestBody SiteSettingDTO siteSettingDTO,
            Authentication authentication
    ) {
        log.info("사이트 설정 수정 - key: {}, value: {}", siteSettingDTO.getSettingkey(), siteSettingDTO.getSettingvalue());

        Map<String, Object> response = new HashMap<>();

        try {
            // 수정자 정보 설정
            if (authentication != null) {
                siteSettingDTO.setUpdatedby(authentication.getName());
            } else {
                siteSettingDTO.setUpdatedby("ADMIN");
            }

            boolean result = siteSettingService.updateSetting(siteSettingDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "설정이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "설정 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("사이트 설정 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "설정 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ========== 보안 설정 관련 API (작성자: 진원, 2025-11-20) ==========

    /**
     * 보안 설정 조회 API
     */
    @GetMapping("/security")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSecuritySettings() {
        log.info("보안 설정 조회");

        Map<String, Object> response = new HashMap<>();

        try {
            List<SecuritySettingDTO> settings = securitySettingService.getAllSettings();

            response.put("success", true);
            response.put("data", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("보안 설정 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "보안 설정 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 보안 설정 수정 API
     */
    @PutMapping("/security")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSecuritySetting(
            @RequestBody SecuritySettingDTO securitySettingDTO,
            Authentication authentication
    ) {
        log.info("보안 설정 수정 - key: {}, value: {}", securitySettingDTO.getSettingkey(), securitySettingDTO.getSettingvalue());

        Map<String, Object> response = new HashMap<>();

        try {
            // 수정자 정보 설정
            if (authentication != null) {
                securitySettingDTO.setUpdatedby(authentication.getName());
            } else {
                securitySettingDTO.setUpdatedby("ADMIN");
            }

            boolean result = securitySettingService.updateSetting(securitySettingDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "보안 설정이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "보안 설정 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("보안 설정 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "보안 설정 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
