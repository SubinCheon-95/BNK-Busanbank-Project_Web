package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.service.CategoryService;
import kr.co.busanbank.service.ProductService;
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
 * 작성일: 2025-11-16
 * 설명: 상품 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/product")
@Controller
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * 상품 관리 페이지
     */
    @GetMapping("")
    public String product(@RequestParam(value = "tab", required = false) String tab, Model model) {
        log.info("Admin Product Page - tab: {}", tab);

        if (tab != null) {
            model.addAttribute("tab", tab);
        }

        return "admin/adminproduct";
    }

    /**
     * 상품 목록 조회 API
     */
    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String productType
    ) {
        log.info("상품 목록 조회 - page: {}, size: {}, keyword: {}, type: {}", page, size, searchKeyword, productType);

        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductDTO> productList = productService.getProductList(page, size, searchKeyword, productType);
            int totalCount = productService.getTotalCount(searchKeyword, productType);
            int totalPages = (int) Math.ceil((double) totalCount / size);

            response.put("success", true);
            response.put("data", productList);
            response.put("totalCount", totalCount);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품 상세 조회 API
     */
    @GetMapping("/products/{productNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable int productNo) {
        log.info("상품 상세 조회 - productNo: {}", productNo);

        Map<String, Object> response = new HashMap<>();

        try {
            ProductDTO product = productService.getProductById(productNo);

            if (product != null) {
                response.put("success", true);
                response.put("data", product);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("상품 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품 추가 API
     */
    @PostMapping("/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("상품 추가 - productName: {}", productDTO.getProductName());

        Map<String, Object> response = new HashMap<>();

        try {
            // 상품명 중복 체크
            if (productService.isProductNameDuplicate(productDTO.getProductName())) {
                response.put("success", false);
                response.put("message", "이미 존재하는 상품명입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = productService.createProduct(productDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "상품이 성공적으로 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품 추가에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("상품 추가 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 추가에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품 수정 API
     */
    @PutMapping("/products/{productNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable int productNo,
            @RequestBody ProductDTO productDTO
    ) {
        log.info("상품 수정 - productNo: {}", productNo);

        Map<String, Object> response = new HashMap<>();

        try {
            productDTO.setProductNo(productNo);
            boolean result = productService.updateProduct(productDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "상품이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("상품 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품 삭제 API
     */
    @DeleteMapping("/products/{productNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable int productNo) {
        log.info("상품 삭제 - productNo: {}", productNo);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = productService.deleteProduct(productNo);

            if (result) {
                response.put("success", true);
                response.put("message", "상품이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품 삭제에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("상품 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품명 중복 체크 API
     */
    @GetMapping("/products/check-name")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkProductName(@RequestParam String productName) {
        log.info("상품명 중복 체크 - productName: {}", productName);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = productService.isProductNameDuplicate(productName);
            response.put("success", true);
            response.put("isDuplicate", isDuplicate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품명 중복 체크 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "중복 체크에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품 관련 카테고리 목록 조회 API
     */
    @GetMapping("/categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategories() {
        log.info("카테고리 목록 조회");

        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryDTO> categories = categoryService.getProductCategories();
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
     * 작성자: 진원
     * 작성일: 2025-11-18
     * 설명: 상품별 가입 유저 목록 조회 API
     */
    @GetMapping("/products/{productNo}/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUsersByProduct(@PathVariable int productNo) {
        log.info("상품별 가입 유저 조회 - productNo: {}", productNo);

        Map<String, Object> response = new HashMap<>();

        try {
            List<UserProductDTO> users = productService.getUsersByProductNo(productNo);
            response.put("success", true);
            response.put("data", users);
            response.put("totalCount", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품별 가입 유저 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품별 가입 유저 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
