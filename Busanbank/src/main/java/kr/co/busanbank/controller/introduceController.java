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
@RequestMapping("/company")
public class introduceController {

    @GetMapping("/company")
    public String company(Model model) {
        return  "company/company";
    }

    @GetMapping("/companyintro")
    public String companyintro(Model model) {
        return  "company/companyintro";
    }

    @GetMapping("/companybankintro")
    public String companybankintro(Model model) {
        return  "company/companybankintro";
    }
    @GetMapping("/companymap")
    public String companymap(Model model) {
        return  "company/companymap";
    }
    @GetMapping("/companystory")
    public String companystory(Model model) {
        return  "company/companystory";
    }
    @GetMapping("/companyinvest")
    public String companyinvest(Model model) {
        return  "company/companyinvest";
    }
    @GetMapping("/adminproduct")
    public String adminproduct(Model model) {
        return  "company/adminproduct";
    }

    @GetMapping("/quizadmincomplete")
    public String quizadmincomplete(Model model) {
        return  "company/quizadmincomplete";
    }

    @GetMapping("/quizdashboardcomplete")
    public String quizdashboardcomplete(Model model) {
        return  "company/quizdashboardcomplete";
    }

    @GetMapping("/quizresultcomplete")
    public String quizresultcomplete(Model model) {
        return  "company/quizresultcomplete";
    }

    @GetMapping("/quizsolvecomplete")
    public String quizsolvecomplete(Model model) {
        return  "company/quizsolvecomplete";
    }

    @GetMapping("/adminproductcategory")
    public String adminproductcategory(Model model) {
        return  "company/adminproductcategory";
    }


    @GetMapping("/adminsetting")
    public String adminsetting(Model model) {
        return  "company/adminsetting";
    }
    //

}
