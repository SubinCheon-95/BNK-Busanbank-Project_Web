package kr.co.busanbank.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/*
    이름: 윤종인
    작성일: 2025-11-22
    설명: 비트코인, 금, 오일 시세 컨트롤러
 */

@RequestMapping("/admin")
@Controller
public class CoinController {

    @GetMapping("/coin-chart")
    public String coinChart() {
        return "admin/marketPrice";
    }
}