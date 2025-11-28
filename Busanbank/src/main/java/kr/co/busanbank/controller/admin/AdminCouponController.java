package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.dto.CouponDTO;
import kr.co.busanbank.service.CategoryService;
import kr.co.busanbank.service.CouponService;
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
 * 작성일: 2025-11-27
 * 설명: 쿠폰 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/coupon")
public class AdminCouponController {

    private final CouponService couponService;
    private final CategoryService categoryService;

    /**
     * 쿠폰 관리 페이지
     */
    @GetMapping
    public String couponPage(Model model) {
        try {
            List<CategoryDTO> categories = categoryService.getProductCategories();
            if (categories == null) {
                categories = new java.util.ArrayList<>();
            }
            model.addAttribute("categories", categories);
            log.info("쿠폰 관리 페이지 로드 - 상품 카테고리 수: {}", categories.size());
            return "admin/admincoupon";
        } catch (Exception e) {
            log.error("쿠폰 관리 페이지 로드 실패: {}", e.getMessage(), e);
            model.addAttribute("categories", new java.util.ArrayList<>());
            return "admin/admincoupon";
        }
    }

    /**
     * 쿠폰 목록 조회 (페이징)
     */
    @GetMapping("/coupons")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCouponList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String isActive
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CouponDTO> couponList = couponService.getCouponList(page, size, searchKeyword, isActive);
            int totalCount = couponService.getCouponCount(searchKeyword, isActive);
            int totalPages = (int) Math.ceil((double) totalCount / size);

            response.put("success", true);
            response.put("data", couponList);
            response.put("totalCount", totalCount);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("쿠폰 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 상세 조회
     */
    @GetMapping("/coupons/{couponId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCoupon(@PathVariable int couponId) {
        Map<String, Object> response = new HashMap<>();
        try {
            CouponDTO coupon = couponService.getCouponById(couponId);
            if (coupon != null) {
                response.put("success", true);
                response.put("data", coupon);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "쿠폰을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("쿠폰 상세 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 코드 중복 체크
     */
    @GetMapping("/check-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCouponCode(@RequestParam String couponCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isDuplicate = couponService.checkCouponCodeDuplicate(couponCode);
            response.put("success", true);
            response.put("isDuplicate", isDuplicate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("쿠폰 코드 중복 체크 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "중복 체크에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 등록
     */
    @PostMapping("/coupons")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCoupon(
            @RequestBody CouponDTO couponDTO,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 관리자 ID 설정 (실제 관리자 ID를 설정해야 함)
            // TODO: Authentication에서 관리자 ID 추출
            // couponDTO.setAdminId(adminId);

            List<Integer> categoryIds = couponDTO.getCategoryIds();
            boolean result = couponService.createCoupon(couponDTO, categoryIds);

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 등록에 실패했습니다. (중복된 쿠폰 코드일 수 있습니다)");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("쿠폰 등록 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 등록 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 수정
     */
    @PutMapping("/coupons/{couponId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCoupon(
            @PathVariable int couponId,
            @RequestBody CouponDTO couponDTO
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            couponDTO.setCouponId(couponId);
            List<Integer> categoryIds = couponDTO.getCategoryIds();
            boolean result = couponService.updateCoupon(couponDTO, categoryIds);

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 수정에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("쿠폰 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 수정 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 삭제
     */
    @DeleteMapping("/coupons/{couponId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCoupon(@PathVariable int couponId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = couponService.deleteCoupon(couponId);

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 삭제에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("쿠폰 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 쿠폰 활성화/비활성화
     */
    @PatchMapping("/coupons/{couponId}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleCouponActive(
            @PathVariable int couponId,
            @RequestParam String isActive
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = couponService.toggleCouponActive(couponId, isActive);

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰 상태가 변경되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 상태 변경에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("쿠폰 상태 변경 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "쿠폰 상태 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 카테고리별 사용 가능 쿠폰 조회
     */
    @GetMapping("/available/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvailableCoupons(@PathVariable int categoryId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CouponDTO> coupons = couponService.getAvailableCouponsByCategory(categoryId);
            response.put("success", true);
            response.put("data", coupons);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카테고리별 쿠폰 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "사용 가능한 쿠폰을 조회할 수 없습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
