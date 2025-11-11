package kr.co.busanbank.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/my")
public class MyController {

    @GetMapping("")
    public String index() {
        return "my/index";
    }

    @GetMapping("/items")
    public String items() {
        return "my/items";
    }



    @GetMapping("/cancel")
    public String cancel() {
        return "my/itemCancel";
    }


    @GetMapping("/cancel/list")
    public String cancelList() {
        return "my/cancelList";
    }

    @GetMapping("/cancel/finish")
    public String cancelFinish() {
        return "my/cancelFinish";
    }




    @GetMapping("/modify")
    public String modify() {
        return "my/infoModify";
    }

    @GetMapping("/change")
    public String change() {
        return "my/pwModify";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "my/withdraw";
    }

    @GetMapping("/withdraw/finish")
    public String withdrawFinish() {
        return "my/withdrawFinish";
    }








}
