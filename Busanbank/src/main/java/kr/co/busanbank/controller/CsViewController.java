package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CategoryDTO;
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

    private final CategoryService categoryService;

    @GetMapping("/cs")
    public String cs() {
        return "cs/cs";
    }

    @GetMapping("/cs/userGuide/nonRegisterProcess")
    public String nonRegisterProcess(Model model) {

        setupPage(37, model);
        return "cs/userGuide/nonRegisterProcess";
    }

    @GetMapping("/cs/userGuide/registerProcess")
    public String registerProcess(Model model) {

        setupPage(38, model);
        return "cs/userGuide/registerProcess";
    }
    @GetMapping("/cs/userGuide/passwordGuide")
    public String passwordGuide(Model model) {

        setupPage(39, model);
        return "cs/userGuide/passwordGuide";
    }
    @GetMapping("/cs/userGuide/serviceAvailable")
    public String serviceAvailable(Model model) {

        setupPage(40, model);
        return "cs/userGuide/serviceAvailable";
    }
    @GetMapping("/cs/userGuide/preferredCustomer")
    public String preferredCustomer(Model model) {

        setupPage(41, model);
        return "cs/userGuide/preferredCustomer";
    }
    @GetMapping("/cs/userGuide/feeGuide")
    public String feeGuide(Model model) {

        setupPage(42, model);
        return "cs/userGuide/feeGuide";
    }

    @GetMapping("/cs/fcqAct/protectionSystem")
    public String protectionSystem(Model model) {

        setupPage(44, model);
        return "cs/fcqAct/protectionSystem";
    }
    @GetMapping("/cs/fcqAct/excellentCase")
    public String excellentCase(Model model) {

        setupPage(54, model);
        return "cs/fcqAct/excellentCase";
    }
    @GetMapping("/cs/fcqAct/caseView")
    public String caseView(Model model) {

        setupPage(54, model);
        return "cs/fcqAct/caseView";
    }

    @GetMapping("/cs/productCenter/manual")
    public String manual(Model model) {

        setupPage(59, model);
        return "cs/productCenter/manual";
    }

    @GetMapping("/cs/productCenter/depositProduct")
    public String depositProduct(Model model) {

        setupPage(61, model);
        return "cs/productCenter/depositProduct";
    }

    @GetMapping("/cs/productCenter/eFinance")
    public String eFinance(Model model) {

        return "cs/productCenter/eFinance";
    }

    @GetMapping("/cs/productCenter/useRate")
    public String useRate(Model model) {

        setupPage(66, model);
        return "cs/productCenter/useRate";
    }

    @GetMapping("/cs/archives/library")
    public String library(Model model) {

        setupPage(68, model);
        return "cs/archives/library";
    }

    private void setupPage(int categoryId, Model model) {
        CategoryDTO current = categoryService.getCategoryById(categoryId);
        List<CategoryDTO> breadcrumb = categoryService.getBreadcrumb(categoryId);
        CategoryDTO root = breadcrumb.get(0);

        List<CategoryDTO> depth2List = categoryService.getChildren(root.getCategoryId());

        Map<Integer, List<CategoryDTO>> depth3Map = new HashMap<>();
        for (CategoryDTO depth2 : depth2List) {
            List<CategoryDTO> children = categoryService.getChildren(depth2.getCategoryId());
            depth3Map.put(depth2.getCategoryId(), children);
        }

        Integer activeDepth2Id;
        if (breadcrumb.size() >= 2) {
            activeDepth2Id = breadcrumb.get(1).getCategoryId();
        } else {
            activeDepth2Id = current.getParentId() != null
                    ? current.getParentId()
                    : current.getCategoryId();
        }

        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("pageTitle", current.getCategoryName());
        model.addAttribute("rootCategory", root);
        model.addAttribute("depth2Menu", depth2List);
        model.addAttribute("depth3Map", depth3Map);
        model.addAttribute("activeDepth2Id", activeDepth2Id);
        model.addAttribute("currentCategoryId", categoryId);
    }
}