package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.dto.ProductTermsDTO;
import kr.co.busanbank.mapper.ProductTermsMapper;
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
 * 작성일: 2025-11-20
 * 설명: 관리자 상품약관 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/productterm")
@Controller
public class AdminProductTermController {

    private final ProductTermsMapper productTermsMapper;
    private final ProductService productService;

    /**
     * 상품약관 작성 페이지
     */
    @GetMapping("/write")
    public String write(@RequestParam int productNo, Model model) {
        log.info("상품약관 작성 페이지 접근 - productNo: {}", productNo);
        model.addAttribute("productNo", productNo);
        return "admin/productterm/write";
    }

    /**
     * 상품약관 수정 페이지
     */
    @GetMapping("/modify")
    public String modify(@RequestParam int termId, Model model) {
        log.info("상품약관 수정 페이지 접근 - termId: {}", termId);
        model.addAttribute("termId", termId);
        return "admin/productterm/modify";
    }

    /**
     * 상품 목록 조회 API (드롭다운용)
     */
    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProducts() {
        log.info("상품 목록 조회 (드롭다운용)");

        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductDTO> products = productService.getAllProducts();
            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "상품 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품별 약관 목록 조회 API
     */
    @GetMapping("/terms/{productNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTermsByProduct(@PathVariable int productNo) {
        log.info("상품별 약관 조회 - productNo: {}", productNo);

        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductTermsDTO> terms = productTermsMapper.selectAllTermsByProductNo(productNo);
            response.put("success", true);
            response.put("data", terms);
            response.put("totalCount", terms.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품별 약관 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 상세 조회 API
     */
    @GetMapping("/term/{termId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTerm(@PathVariable int termId) {
        log.info("약관 상세 조회 - termId: {}", termId);

        Map<String, Object> response = new HashMap<>();

        try {
            ProductTermsDTO term = productTermsMapper.selectTermById(termId);

            if (term != null) {
                response.put("success", true);
                response.put("data", term);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("약관 상세 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품약관 추가 API
     */
    @PostMapping("/terms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTerm(@RequestBody ProductTermsDTO productTermsDTO) {
        log.info("상품약관 추가 - productNo: {}, termTitle: {}",
                productTermsDTO.getProductNo(), productTermsDTO.getTermTitle());

        Map<String, Object> response = new HashMap<>();

        try {
            // displayOrder가 설정되지 않은 경우 자동 설정
            if (productTermsDTO.getDisplayOrder() == 0) {
                int maxOrder = productTermsMapper.selectMaxDisplayOrder(productTermsDTO.getProductNo());
                productTermsDTO.setDisplayOrder(maxOrder + 1);
            }

            int result = productTermsMapper.insertProductTerm(productTermsDTO);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 추가에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 추가 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 추가에 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품약관 수정 API
     */
    @PutMapping("/terms/{termId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTerm(
            @PathVariable int termId,
            @RequestBody ProductTermsDTO productTermsDTO
    ) {
        log.info("상품약관 수정 - termId: {}", termId);

        Map<String, Object> response = new HashMap<>();

        try {
            productTermsDTO.setTermId(termId);
            int result = productTermsMapper.updateProductTerm(productTermsDTO);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 상품약관 삭제 API
     */
    @DeleteMapping("/terms/{termId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTerm(@PathVariable int termId) {
        log.info("상품약관 삭제 - termId: {}", termId);

        Map<String, Object> response = new HashMap<>();

        try {
            int result = productTermsMapper.deleteProductTerm(termId);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 삭제에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
