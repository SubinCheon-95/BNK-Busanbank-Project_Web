package kr.co.busanbank.service;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-18
 * 설명: 카테고리 관리 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 모든 활성 카테고리 조회
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryMapper.selectAllCategories();
    }

    /**
     * 상품 관련 카테고리만 조회
     */
    public List<CategoryDTO> getProductCategories() {
        return categoryMapper.selectProductCategories();
    }

    /**
     * 카테고리 ID로 조회
     */
    public CategoryDTO getCategoryById(int categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    /**
     * 카테고리 추가
     */
    public boolean createCategory(CategoryDTO categoryDTO) {
        try {
            int result = categoryMapper.insertCategory(categoryDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("카테고리 추가 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 카테고리 수정
     */
    public boolean updateCategory(CategoryDTO categoryDTO) {
        try {
            int result = categoryMapper.updateCategory(categoryDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("카테고리 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 카테고리 삭제
     */
    public boolean deleteCategory(int categoryId) {
        try {
            int result = categoryMapper.deleteCategory(categoryId);
            return result > 0;
        } catch (Exception e) {
            log.error("카테고리 삭제 실패: {}", e.getMessage());
            return false;
        }
    }
  
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
