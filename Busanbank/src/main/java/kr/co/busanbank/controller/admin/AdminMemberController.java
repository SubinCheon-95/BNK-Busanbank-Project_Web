package kr.co.busanbank.controller.admin;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
<<<<<<< HEAD:Busanbank/src/main/java/kr/co/busanbank/project/controller/productController.java
@RequestMapping("/product")
public class productController {

    @GetMapping("/list")
    public String list(Model model) {
        return  "product/list";
    }

    @GetMapping("/view")
    public String view(Model model) {
        return  "product/prodView";
    }
=======
@RequestMapping("/admin/member")
@Controller
public class AdminMemberController {

    @GetMapping("/list")
    public String list(Model model) {return "admin/member/adminMemberList";}
>>>>>>> d38cc0ec3767e9229bbaa3c3ba03012f0aed2246:Busanbank/src/main/java/kr/co/busanbank/controller/admin/AdminMemberController.java
}
