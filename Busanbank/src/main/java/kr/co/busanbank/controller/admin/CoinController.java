package kr.co.busanbank.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoinController {

    @GetMapping("/coin-chart")
    public String coinChart() {
        return "admin/coin";
    }
}