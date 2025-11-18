package kr.co.busanbank.service;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryDTO> getBreadcrumb(int categoryId) {

        List<CategoryDTO> breadcrumb = new ArrayList<>();

        CategoryDTO current = categoryMapper.findById(categoryId);
        breadcrumb.add(current);

        while (current.getParentId() != null) {
            current = categoryMapper.findById(current.getParentId());
            breadcrumb.add(current);
        }

        Collections.reverse(breadcrumb);
        return breadcrumb;
    }

    public List<CategoryDTO> getDepth1Categories() {
        return categoryMapper.findDepth1();
    }

    public List<CategoryDTO> getChildren(int parentId) {
        return categoryMapper.findChildren(parentId);
    }

    public String getPageTitle(int categoryId) {
        return categoryMapper.findById(categoryId).getCategoryName();
    }
}