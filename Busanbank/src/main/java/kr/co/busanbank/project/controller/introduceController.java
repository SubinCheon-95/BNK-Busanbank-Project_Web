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
    @GetMapping("/adminproductList")
    public String adminproductList(Model model) {
        return  "company/adminproductList";
    }
    @GetMapping("/adminaccount")
    public String adminaccount(Model model) {
        return  "company/adminaccount";
    }
    @GetMapping("/adminsecurity")
    public String adminsecurity(Model model) {
        return  "company/adminsecurity";
    }
    @GetMapping("/admincertificate")
    public String admincertificate(Model model) {
        return  "company/admincertificate";
    }
    @GetMapping("/adminsite")
    public String adminsite(Model model) {
        return  "company/adminsite";
    }
    @GetMapping("/adminpolicy")
    public String adminpolicy(Model model) {
        return  "company/adminpolicy";
    }
    @GetMapping("/adminapi")
    public String adminapi(Model model) {
        return  "company/adminapi";
    }

}
