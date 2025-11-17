package kr.co.busanbank.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AppInfo appInfo;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appInfo", appInfo);
    }
}
