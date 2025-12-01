/*
    수정일 : 2025/11/28
    수정자 : 천수빈
    내용 : 헤더 카테고리 데이터 추가
*/
package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.service.ProductService;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
//    private final CategoryService categoryService;  // 헤더 카테고리 데이터 추가 25.11.28_수빈

    /*
     * 모든 페이지에서 사용할 헤더 카테고리 데이터
     */
//    @ModelAttribute("headerCategories")
//    public Map<String, Object> getHeaderCategories() {
//        Map<String, Object> headerData = new HashMap<>();
//
//        try {
//            // 예금상품 (CATEGORYID=1의 하위 카테고리)
//            List<CategoryDTO> depositProducts = categoryService.getCategoriesByParentId(1);
//            headerData.put("depositProducts", depositProducts);
//            log.info("예금상품 카테고리 로드: {} 개", depositProducts.size());
//
//            // 예금이용가이드 (CATEGORYID=12의 하위 카테고리)
//            List<CategoryDTO> depositGuides = categoryService.getCategoriesByParentId(12);
//            headerData.put("depositGuides", depositGuides);
//            log.info("예금이용가이드 카테고리 로드: {} 개", depositGuides.size());
//
//            // 금융서비스 (CATEGORYID=15의 하위 카테고리)
//            List<CategoryDTO> financialServices = categoryService.getCategoriesByParentId(15);
//            headerData.put("financialServices", financialServices);
//            log.info("금융서비스 카테고리 로드: {} 개", financialServices.size());
//
//        } catch (Exception e) {
//            log.error("헤더 카테고리 로드 실패: {}", e.getMessage());
//            headerData.put("depositProducts", new ArrayList<>());
//            headerData.put("depositGuides", new ArrayList<>());
//            headerData.put("financialServices", new ArrayList<>());
//        }
//
//        return headerData;
//    }


    @GetMapping("/")
    public String home(Model model) {

        // 메인에 보여줄 상품 번호
        List<ProductDTO> products = productService.getTopProducts(6);

        // 디버그 로그 추가 25.11.26_수빈
        if (!products.isEmpty()) {
            ProductDTO firstProduct = products.get(0);
            log.info("첫 번째 상품: {}", firstProduct.getProductName());
            log.info("joinTypes: {}", firstProduct.getJoinTypes());
            log.info("joinTypesStr: {}", firstProduct.getJoinTypesStr());
        }

        // index.html로 전달
        model.addAttribute("products", products);

        return "index";
    }
}
