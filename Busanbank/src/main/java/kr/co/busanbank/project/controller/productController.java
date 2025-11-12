package kr.co.busanbank.project.controller;

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

    // 상품리스트
    @GetMapping("/list")
    public String list(Model model) {
        return  "product/lumpSumList";
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
