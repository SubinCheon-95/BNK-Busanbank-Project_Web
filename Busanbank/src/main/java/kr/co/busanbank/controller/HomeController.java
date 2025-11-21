package kr.co.busanbank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.service.ProductService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {

        // 메인에 보여줄 상품 번호
        List<Integer> mainProductIds = List.of(105, 103, 101, 104);

        // 상품 조회
        List<ProductDTO> products = productService.getProductsByIds(mainProductIds);

        // index.html로 전달
        model.addAttribute("products", products);

        return "index";
    }
}
