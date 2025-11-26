/*
    수정일 : 2025/11/26
    수정자 : 천수빈
    내용 : 디버그 로그 추가
*/
package kr.co.busanbank.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.service.ProductService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

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
