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
        return  "prod/list";
    }

    // 상품상세정보
    @GetMapping("/view")
    public String view(Model model) {
        return  "prod/view";
    }


    // 상품가입
    @GetMapping("/register")
    public String view(Model model) {
        return  "prod/view";
    }



}
