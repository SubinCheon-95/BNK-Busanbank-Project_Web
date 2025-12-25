/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 고객센터 전용 헤더 GNB 적용
*/
package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.helper.CategoryPageHelper;
import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CsViewController {

    private final CategoryPageHelper categoryPageHelper;
    private final CategoryService categoryService;

    /* 25.11.29_수빈 */
    @ModelAttribute("csHeaderCategories")
    public Map<String, Object> getCsHeaderCategories() {
        Map<String, Object> headerData = new HashMap<>();

        try {
            // 고객상담 (CATEGORYID=30의 하위)
            List<CategoryDTO> customerSupport = categoryService.getCategoriesByParentId(30);
            headerData.put("customerSupport", customerSupport);

            // 이용안내 (CATEGORYID=35의 하위)
            List<CategoryDTO> usageGuide = categoryService.getCategoriesByParentId(35);
            headerData.put("usageGuide", usageGuide);

            // 금융소비자보호 (CATEGORYID=43의 하위)
            List<CategoryDTO> consumerProtection = categoryService.getCategoriesByParentId(43);
            headerData.put("consumerProtection", consumerProtection);

            // 상품공시실 (CATEGORYID=58의 하위)
            List<CategoryDTO> productDisclosure = categoryService.getCategoriesByParentId(58);
            headerData.put("productDisclosure", productDisclosure);

            // 서식/약관/자료실 (CATEGORYID=67의 하위)
            List<CategoryDTO> archives = categoryService.getCategoriesByParentId(67);
            headerData.put("archives", archives);

//            log.info("고객센터 헤더 카테고리 로드 - 고객상담:{}, 이용안내:{}, 소비자보호:{}, 상품공시:{}, 서식자료:{}",
//                    customerSupport.size(), usageGuide.size(),
//                    consumerProtection.size(), productDisclosure.size(), archives.size());

        } catch (Exception e) {
            log.error("고객센터 헤더 카테고리 로드 실패: {}", e.getMessage());
            headerData.put("customerSupport", new ArrayList<>());
            headerData.put("usageGuide", new ArrayList<>());
            headerData.put("consumerProtection", new ArrayList<>());
            headerData.put("productDisclosure", new ArrayList<>());
            headerData.put("archives", new ArrayList<>());
        }

        return headerData;
    }

    @GetMapping({"/cs", "/cs/"})
    public String cs() {
        return "cs/cs";
    }

    @GetMapping("/cs/userGuide/nonRegisterProcess")
    public String nonRegisterProcess(Model model) {

        categoryPageHelper.setupPage(37, model);
        return "cs/userGuide/nonRegisterProcess";
    }

    @GetMapping("/cs/userGuide/registerProcess")
    public String registerProcess(Model model) {

        categoryPageHelper.setupPage(38, model);
        return "cs/userGuide/registerProcess";
    }
    @GetMapping("/cs/userGuide/passwordGuide")
    public String passwordGuide(Model model) {

        categoryPageHelper.setupPage(39, model);
        return "cs/userGuide/passwordGuide";
    }
    @GetMapping("/cs/userGuide/serviceAvailable")
    public String serviceAvailable(Model model) {

        categoryPageHelper.setupPage(40, model);
        return "cs/userGuide/serviceAvailable";
    }
    @GetMapping("/cs/userGuide/preferredCustomer")
    public String preferredCustomer(Model model) {

        categoryPageHelper.setupPage(41, model);
        return "cs/userGuide/preferredCustomer";
    }
    @GetMapping("/cs/userGuide/feeGuide")
    public String feeGuide(Model model) {

        categoryPageHelper.setupPage(42, model);
        return "cs/userGuide/feeGuide";
    }

    @GetMapping("/cs/fcqAct/protectionSystem")
    public String protectionSystem(Model model) {

        categoryPageHelper.setupPage(44, model);
        return "cs/fcqAct/protectionSystem";
    }
    @GetMapping("/cs/fcqAct/excellentCase")
    public String excellentCase(PageRequestDTO pageRequestDTO, Model model) {
        // pageRequestDTO를 Model에 추가
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        categoryPageHelper.setupPage(54, model);
        return "cs/fcqAct/excellentCase";
    }
    @GetMapping("/cs/fcqAct/caseView")
    public String caseView(Model model) {

        categoryPageHelper.setupPage(54, model);
        return "cs/fcqAct/caseView";
    }

    @GetMapping("/cs/productCenter/manual")
    public String manual(Model model) {

        categoryPageHelper.setupPage(59, model);
        return "cs/productCenter/manual";
    }

    @GetMapping("/cs/productCenter/depositProduct")
    public String depositProduct(PageRequestDTO pageRequestDTO, Model model) {

        // pageRequestDTO를 Model에 추가
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        categoryPageHelper.setupPage(61, model);
        return "cs/productCenter/depositProduct";
    }

    @GetMapping("/cs/productCenter/eFinance")
    public String eFinance(Model model) {

        return "cs/productCenter/eFinance";
    }

    @GetMapping("/cs/productCenter/useRate")
    public String useRate(Model model) {

        categoryPageHelper.setupPage(66, model);
        return "cs/productCenter/useRate";
    }

    @GetMapping("/cs/archives/library")
    public String library(Model model) {

        categoryPageHelper.setupPage(68, model);
        return "cs/archives/library";
    }
    
}