package kr.co.busanbank.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CsController {

    @GetMapping("/cs")
    public String cs() {
        return "cs/cs";
    }

    @GetMapping("/cs/customerSupport/faq")
    public String faq() {
        return "cs/customerSupport/faq";
    }

    @GetMapping("/cs/customerSupport/necessaryDocu")
    public String necessaryDocu() {
        return "cs/customerSupport/necessaryDocu";
    }

    @GetMapping("/cs/customerSupport/docuView")
    public String docuView() {
        return "cs/customerSupport/docuView";
    }

    @GetMapping("/cs/customerSupport/onlineCounsel")
    public String onlineCounsel() {
        return "cs/customerSupport/onlineCounsel";
    }

    @GetMapping("/cs/customerSupport/talkCounsel")
    public String talkCounsel() {
        return "cs/customerSupport/talkCounsel";
    }

    @GetMapping("cs/userGuide/nonRegisterProcess")
    public String nonRegisterProcess() {
        return "cs/userGuide/nonRegisterProcess";
    }

    @GetMapping("cs/userGuide/registerProcess")
    public String registerProcess() {
        return "cs/userGuide/registerProcess";
    }
    @GetMapping("cs/userGuide/passwordGuide")
    public String passwordGuide() {
        return "cs/userGuide/passwordGuide";
    }
    @GetMapping("cs/userGuide/serviceAvailable")
    public String serviceAvailable() {
        return "cs/userGuide/serviceAvailable";
    }
    @GetMapping("cs/userGuide/preferredCustomer")
    public String preferredCustomer() {
        return "cs/userGuide/preferredCustomer";
    }
    @GetMapping("cs/userGuide/feeGuide")
    public String feeGuide() {
        return "cs/userGuide/feeGuide";
    }

    @GetMapping("cs/fcqAct/protectionSystem")
    public String protectionSystem() {
        return "cs/fcqAct/protectionSystem";
    }
    @GetMapping("cs/fcqAct/excellentCase")
    public String excellentCase() {
        return "cs/fcqAct/excellentCase";
    }
    @GetMapping("cs/fcqAct/caseView")
    public String caseView() {
        return "cs/fcqAct/caseView";
    }

    @GetMapping("cs/productCenter/manual")
    public String manual() {
        return "cs/productCenter/manual";
    }

    @GetMapping("cs/productCenter/depositProduct")
    public String depositProduct() {
        return "cs/productCenter/depositProduct";
    }

    @GetMapping("cs/productCenter/eFinance")
    public String eFinance() {
        return "cs/productCenter/eFinance";
    }

    @GetMapping("cs/productCenter/useRate")
    public String useRate() {
        return "cs/productCenter/useRate";
    }

    @GetMapping("cs/archives/library")
    public String library() {
        return "cs/archives/library";
    }


}
