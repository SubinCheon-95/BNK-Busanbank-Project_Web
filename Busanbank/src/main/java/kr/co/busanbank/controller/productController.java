package kr.co.busanbank.controller;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.dto.ProductDetailDTO;
import kr.co.busanbank.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 날짜 : 202511/21
 * 이름 : 김수진
 * ***********************************************
 * 내용 :         ProductController
 ************************************************ */

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

    // 회원상품가입 ===========> 이 부분은 productjoincontroller로 이동함
//    // STEP 1: 각종 동의
//    @GetMapping("/productjoin")
//    public String showStep1(Model model) {
//        log.info("STEP 1 호출");
//        return "product/productJoinStage/registerstep01";  // templates/product/productJoinStage/registerstep01.html
//    }
//
//    // STEP 2: 정보입력
//    @GetMapping("/productjoin/step2")
//    public String showStep2(Model model) {
//        log.info("STEP 2 호출");
//        return "product/productJoinStage/registerstep02";  // templates/product/productJoinStage/registerstep02.html
//    }
//
//    // STEP 3: 이율안내및 또 동의
//    @GetMapping("/productjoin/step3")
//    public String showStep3(Model model) {
//        log.info("STEP 3 호출");
//        return "product/productJoinStage/registerstep03";  // templates/product/productJoinStage/registerstep03.html
//    }
//
//    // STEP 4: 최최최최종확인
//    @GetMapping("/productjoin/step4")
//    public String showStep4(Model model) {
//        log.info("STEP 4 호출");
//        return "product/productJoinStage/registerstep04";  // templates/product/productJoinStage/registerstep04.html
//    }

    // ★★★ 상품 상세, productdetail 컨트롤러 ★★★
    @GetMapping("/view")
    public String view(
            @RequestParam("productNo") int productNo,
            @RequestParam(value = "error", required = false) String error,
            Model model) {

        log.info("상품 상세 조회 - productNo: {}, error: {}", productNo, error);

        // 기본 상품 정보 조회
        ProductDTO product = productService.getProductById(productNo);

        // 상품 상세 정보 조회
        ProductDetailDTO detail = productService.getProductDetail(productNo);

        if (product == null) {
            log.error("상품을 찾을 수 없습니다 - productNo: {}", productNo);
            return "error/404";
        }

        model.addAttribute("product", product);
        model.addAttribute("detail", detail);

        // ✅ 에러 메시지 처리 추가
        if ("password".equals(error)) {
            model.addAttribute("errorMessage", "계좌 비밀번호가 일치하지 않습니다.\n다시 시도해주세요.");
            model.addAttribute("showErrorModal", true);
            log.warn("계좌 비밀번호 불일치로 인한 리다이렉트");
        } else if ("system".equals(error)) {
            model.addAttribute("errorMessage", "시스템 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
            model.addAttribute("showErrorModal", true);
            log.error("시스템 오류로 인한 리다이렉트");
        }

        log.info("상품 정보: {}", product);
        log.info("상세 정보: {}", detail);

        return "product/prodView";
    }

    // 키워드 검색(+페이지네이션)
    @GetMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {

        log.info("키워드 검색 keyword = {}, page = {}", keyword, page);

        int offset = (page - 1) * size;

        // 1) 페이지 데이터 조회
        List<ProductDTO> products = productService.searchProductsPaged(keyword, offset, size);

        // 2) 전체 개수 조회
        int totalCount = productService.countSearchResults(keyword);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 3) 페이지 그룹 계산 (5개씩)
        int groupSize = 5;
        int currentGroup = (page - 1) / groupSize;

        int startPage = currentGroup * groupSize + 1;
        int endPage = startPage + groupSize - 1;
        if (endPage > totalPages) endPage = totalPages;

        // 4) 뷰로 전달
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", totalCount);

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "product/productSearchResult";
    }
}