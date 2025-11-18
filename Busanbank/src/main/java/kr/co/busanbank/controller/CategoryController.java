package kr.co.busanbank.controller;

import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/page")
    public String loadPage(@RequestParam int categoryId,
                           @RequestParam String view,
                           Model model) {

        model.addAttribute("breadcrumb", categoryService.getBreadcrumb(categoryId));
        model.addAttribute("pageTitle", categoryService.getPageTitle(categoryId));

        return view;
    }
}
