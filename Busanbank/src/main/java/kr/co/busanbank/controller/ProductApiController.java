package kr.co.busanbank.controller;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {


    private final ProductService productService;

    /**
     * 상품 목록 조회 (전체 또는 categoryId별)
     *  - /api/products                 → 전체
     *  - /api/products?categoryId=3    → 카테고리 3번
     */
    @GetMapping
    public List<ProductDTO> list(@RequestParam(required = false) Integer categoryId) {

        if (categoryId == null) {
            // ✅ 전체 상품: ProductService
            return productService.getAllProducts();
        } else {
            // ✅ 카테고리별 상품: ProductService
            return productService.getProductsByCategory(categoryId);
        }
    }

    /**
     * 상품 상세 조회
     *  - /api/products/12
     */
    @GetMapping("/{productNo}")
    public ProductDTO detail(@PathVariable int productNo) {
        // ✅ 상품 상세: ProductService
        return productService.getProductById(productNo);
    }
}
