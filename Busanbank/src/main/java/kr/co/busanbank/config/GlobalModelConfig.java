package kr.co.busanbank.config;

import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ModelAttribute;

@Configuration
@RequiredArgsConstructor
public class GlobalModelConfig {

    private final CategoryService categoryService;

    @ModelAttribute("depth1Categories")
    public Object depth1Categories() {
        return categoryService.getDepth1Categories();
    }
}
