package kr.co.busanbank.controller;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/prod")
public class productController {

    private final ProductService productService;

    // 화면띄우는 실험용 컨트롤러
    @GetMapping("/index")
    public String index(Model model) {
        return  "product/productJoinStage/productindex";
    }


    // 상품리스트 - 전체 메인페이지
    @GetMapping("/list/main")
    public String list(Model model) {
        return "product/productMain";
    }


    // ★★★ 상품리스트 - 입출금자유 (CATEGORYID = 6) ★★★
    @GetMapping("/list/freedepwith")
    public String showList1(Model model) {
        log.info("list freedepwith 호출 - CATEGORYID = 3");

        List<ProductDTO> products = productService.getProductsByCategory(3);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());

        log.info("입출금자유 상품 개수: {}", products.size());

        return "product/freeDepWith";
    }

    // ★★★ 상품리스트 - 주택마련 (CATEGORYID = 10) ★★★
    @GetMapping("/list/housing")
    public String showList2(Model model) {
        log.info("list housing 호출 - CATEGORYID = 7");

        List<ProductDTO> products = productService.getProductsByCategory(7);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());

        log.info("주택마련 상품 개수: {}", products.size());

        return "product/housingPurchase";
    }

    // ★★★ 상품리스트 - 목돈굴리기 (CATEGORYID = 9) ★★★
    @GetMapping("/list/lumprolling")
    public String showList3(Model model) {
        log.info("list lumprolling 호출 - CATEGORYID = 6");

        List<ProductDTO> products = productService.getProductsByCategory(6);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());

        log.info("목돈굴리기 상품 개수: {}", products.size());

        return "product/lumpRollingList";
    }

    // ★★★ 상품리스트 - 목돈만들기 (CATEGORYID = 8) ★★★
    @GetMapping("/list/lumpsum")
    public String showList4(Model model) {
        log.info("list lumpsum 호출 - CATEGORYID = 5");

        List<ProductDTO> products = productService.getProductsByCategory(5);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());

        log.info("목돈만들기 상품 개수: {}", products.size());

        return "product/lumpSumList";
    }

    // ★★★ 상품리스트 - 스마트금융전용 (CATEGORYID = 11) ★★★
    @GetMapping("/list/smartfinance")
    public String showList5(Model model) {
        log.info("list smartfinance 호출 - CATEGORYID = 8");

        List<ProductDTO> products = productService.getProductsByCategory(8);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());

        log.info("스마트금융전용 상품 개수: {}", products.size());

        return "product/smartFinance";
    }

    // 회원상품가입
    // STEP 1: 각종 동의
    @GetMapping("/productjoin")
    public String showStep1(Model model) {
        log.info("STEP 1 호출");
        return "product/productJoinStage/registerstep01";  // templates/product/productJoinStage/registerstep01.html
    }

    // STEP 2: 정보입력
    @GetMapping("/productjoin/step2")
    public String showStep2(Model model) {
        log.info("STEP 2 호출");
        return "product/productJoinStage/registerstep02";  // templates/product/productJoinStage/registerstep02.html
    }

    // STEP 3: 이율안내및 또 동의
    @GetMapping("/productjoin/step3")
    public String showStep3(Model model) {
        log.info("STEP 3 호출");
        return "product/productJoinStage/registerstep03";  // templates/product/productJoinStage/registerstep03.html
    }

    // STEP 4: 최최최최종확인
    @GetMapping("/productjoin/step4")
    public String showStep4(Model model) {
        log.info("STEP 4 호출");
        return "product/productJoinStage/registerstep04";  // templates/product/productJoinStage/registerstep04.html
    }

    // 상품 상세
    @GetMapping("/view")
    public String view(@RequestParam("id") int id, Model model) {
        return "product/prodView";
    }


    // ★ 키워드 검색
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {

        log.info("키워드 검색 keyword = {}", keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("products", productService.searchProducts(keyword));

        return "product/productSearchResult";
    }
}