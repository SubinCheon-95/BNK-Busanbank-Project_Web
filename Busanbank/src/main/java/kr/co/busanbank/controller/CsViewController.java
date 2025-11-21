package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.helper.CategoryPageHelper;
import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CsViewController {

    private final CategoryPageHelper categoryPageHelper;

    private final CategoryService categoryService;

    @GetMapping("/cs")
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
    public String excellentCase(Model model) {

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
    public String depositProduct(Model model) {

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