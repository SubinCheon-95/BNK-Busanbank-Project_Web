package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.service.CategoryService;
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
 * 작성일: 2025-11-18
 * 설명: 카테고리 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/category")
@Controller
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 관리 페이지
     */
    @GetMapping("")
    public String category(Model model) {
        log.info("Admin Category Management Page");
        return "admin/admincategory";
    }

    /**
     * 전체 카테고리 목록 조회 API
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategoryList() {
        log.info("카테고리 목록 조회");

        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카테고리 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "카테고리 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 카테고리 상세 조회 API
     */
    @GetMapping("/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategory(@PathVariable int categoryId) {
        log.info("카테고리 상세 조회 - categoryId: {}", categoryId);

        Map<String, Object> response = new HashMap<>();

        try {
            CategoryDTO category = categoryService.getCategoryById(categoryId);

            if (category != null) {
                response.put("success", true);
                response.put("data", category);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "카테고리를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("카테고리 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "카테고리 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 카테고리 추가 API
     */
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("카테고리 추가 - categoryName: {}", categoryDTO.getCategoryName());

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = categoryService.createCategory(categoryDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "카테고리가 성공적으로 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "카테고리 추가에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("카테고리 추가 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "카테고리 추가에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 카테고리 수정 API
     */
    @PutMapping("/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable int categoryId,
            @RequestBody CategoryDTO categoryDTO
    ) {
        log.info("카테고리 수정 - categoryId: {}", categoryId);

        Map<String, Object> response = new HashMap<>();

        try {
            categoryDTO.setCategoryId(categoryId);
            boolean result = categoryService.updateCategory(categoryDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "카테고리가 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "카테고리 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("카테고리 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "카테고리 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 카테고리 삭제 API
     */
    @DeleteMapping("/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable int categoryId) {
        log.info("카테고리 삭제 - categoryId: {}", categoryId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = categoryService.deleteCategory(categoryId);

            if (result) {
                response.put("success", true);
                response.put("message", "카테고리가 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "카테고리 삭제에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("카테고리 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "카테고리 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
