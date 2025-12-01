/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 고객센터 전용 헤더 GNB 적용
*/
package kr.co.busanbank.service;

import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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


    /*
        25.11.28_수빈
        헤더용 카테고리 데이터 조회
        @return 헤더에 필요한 카테고리 맵
    */
    public Map<String, List<CategoryDTO>> getHeaderCategories() {
        Map<String, List<CategoryDTO>> headerData = new HashMap<>();

        // 예금상품 (PARENTID = 1)
        List<CategoryDTO> depositProducts = categoryMapper.findChildren(1);
        headerData.put("depositProducts", depositProducts);

        // 금융서비스 (PARENTID = 2) - parentId 확인 필요
        List<CategoryDTO> financialServices = categoryMapper.findChildren(2);
        headerData.put("financialServices", financialServices);

        return headerData;
    }

    /*
        25.11.29_수빈
        특정 부모 ID의 자식 카테고리 조회 (헤더용)
        routePath 자동 설정 포함
    */
    public List<CategoryDTO> getCategoriesByParentId(int parentId) {
        try {
            List<CategoryDTO> categories = categoryMapper.findChildren(parentId);

            // ✅ 각 카테고리에 routePath 설정
            for (CategoryDTO category : categories) {
                String path = buildRoutePath(parentId, category.getCategoryId());
                category.setRoutePath(path);
            }

            log.info("ParentId {} 하위 카테고리 조회: {} 개", parentId, categories.size());
            return categories != null ? categories : new ArrayList<>();


        } catch (Exception e) {
            log.error("카테고리 조회 실패 (ParentId: {}): {}", parentId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /*
     * 부모 카테고리와 자식 카테고리 ID를 기반으로 routePath 생성
     */
    private String buildRoutePath(int parentId, int categoryId) {
        String basePath = getBasePath(parentId);
        String pagePath = getPagePath(categoryId);
        return basePath + pagePath;
    }

    /*
     * 부모 카테고리에 따른 기본 경로
     */
    private String getBasePath(int parentId) {
        switch (parentId) {
            case 30: return "/cs/customerSupport/";
            case 35: return "/cs/userGuide/";
            case 43: return "/cs/fcqAct/";
            case 58: return "/cs/productCenter/";
            case 67: return "/cs/archives/";
            default: return "/cs/";
        }
    }

    /*
     * 카테고리 ID에 따른 페이지 경로
     */
    private String getPagePath(int categoryId) {
        switch (categoryId) {
            // 고객상담 (parentId: 30)
            case 31: return "faq";
            case 32: return "necessaryDocu";
            case 33: return "login/talkCounsel";
            case 34: return "login/onlineCounsel";

            // 이용안내 (parentId: 35)
            case 36: return "nonRegisterProcess";
            case 37: return "registerProcess";
            case 38: return "passwordGuide";
            case 39: return "serviceAvailable";
            case 40: return "preferredCustomer";
            case 41: return "feeGuide";
            case 42: return "feeGuide";

            // 금융소비자보호 (parentId: 43)
            case 44: return "protectionSystem";
            case 54: return "excellentCase";

            // 상품공시실 (parentId: 58)
            case 59: return "manual";
            case 61: return "depositProduct";
            case 66: return "useRate";

            // 서식/약관/자료실 (parentId: 67)
            case 68: return "library";

            default:
                log.warn("매핑되지 않은 카테고리 ID: {}", categoryId);
                return "unknown";
        }
    }
}
