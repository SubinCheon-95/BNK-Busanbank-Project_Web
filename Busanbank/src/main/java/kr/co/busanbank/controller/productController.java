package kr.co.busanbank.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/prod")
public class productController {

    // 화면띄우는 실험용 컨트롤러
    @GetMapping("/index")
    public String index(Model model) {
        return  "product/productJoinStage/productindex";
    }

    // 상품리스트 - 전체 메인페이지
    @GetMapping("/list/main")
    public String list(Model model) {
        return  "product/productMain";
    }


    // 상품리스트 - 자유예금
    @GetMapping("/list/freedepwith")
    public String showList1(Model model) {
        log.info("list freedepwith 호출");
        return "product/freeDepWith";
    }

    // 상품리스트 - 주택마련
    @GetMapping("/list/housing")
    public String showList2(Model model) {
        log.info("list housing 호출");
        return "product/housingPurchase";
    }

    // 상품리스트 - 목돈굴리기
    @GetMapping("/list/lumprolling")
    public String showList3(Model model) {
        log.info("list lumprolling 호출");
        return "product/lumpRollingList";
    }

    // 상품리스트 - 목돈만들기
    @GetMapping("/list/lumpsum")
    public String showList4(Model model) {
        log.info("list lumpsum 호출");
        return "product/lumpSumList";
    }

    // 상품리스트 - 스마트금융전용
    @GetMapping("/list/smartfinance")
    public String showList5(Model model) {
        log.info("list smartfinance 호출");
        return "product/smartFinance";
    }


    // 상품상세정보
    @GetMapping("/view")
    public String view(Model model) {
        return  "product/prodView";
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

}