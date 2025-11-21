package kr.co.busanbank.helper;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CategoryPageHelper {

    private final CategoryService categoryService;

    public void setupPage(int categoryId, Model model) {

        CategoryDTO current = categoryService.getCategoryById(categoryId);
        List<CategoryDTO> breadcrumb = categoryService.getBreadcrumb(categoryId);
        CategoryDTO root = breadcrumb.get(0);

        List<CategoryDTO> depth2List = categoryService.getChildren(root.getCategoryId());

        Map<Integer, List<CategoryDTO>> depth3Map = new HashMap<>();
        for (CategoryDTO depth2 : depth2List) {
            List<CategoryDTO> children = categoryService.getChildren(depth2.getCategoryId());
            depth3Map.put(depth2.getCategoryId(), children);
        }

        Integer activeDepth2Id;
        if (breadcrumb.size() >= 2) {
            activeDepth2Id = breadcrumb.get(1).getCategoryId();
        } else {
            activeDepth2Id = current.getParentId() != null
                    ? current.getParentId()
                    : current.getCategoryId();
        }

        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("pageTitle", current.getCategoryName());
        model.addAttribute("rootCategory", root);
        model.addAttribute("depth2Menu", depth2List);
        model.addAttribute("depth3Map", depth3Map);
        model.addAttribute("activeDepth2Id", activeDepth2Id);
        model.addAttribute("currentCategoryId", categoryId);
    }
}
