package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-18
 * 설명: 카테고리 관리 Mapper 인터페이스
 */
@Mapper
public interface CategoryMapper {

    // 모든 활성 카테고리 조회
    List<CategoryDTO> selectAllCategories();

    // 상품 관련 카테고리만 조회 (예금상품 하위)
    List<CategoryDTO> selectProductCategories();

    // 카테고리 ID로 조회
    CategoryDTO selectCategoryById(@Param("categoryId") int categoryId);

    // 카테고리 추가
    int insertCategory(CategoryDTO categoryDTO);

    // 카테고리 수정
    int updateCategory(CategoryDTO categoryDTO);

    // 카테고리 삭제 (soft delete)
    int deleteCategory(@Param("categoryId") int categoryId);

    CategoryDTO findById(int categoryId);

    List<CategoryDTO> findDepth1();

    List<CategoryDTO> findChildren(int parentId);

}
