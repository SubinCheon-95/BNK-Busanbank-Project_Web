package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 설명: 금융상품 관리 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 상품 목록 조회 (페이징)
     */
    public List<ProductDTO> getProductList(int page, int size, String searchKeyword, String productType) {
        int offset = (page - 1) * size;
        return productMapper.selectProductList(offset, size, searchKeyword, productType);
    }

    /**
     * 상품 전체 개수
     */
    public int getTotalCount(String searchKeyword, String productType) {
        return productMapper.countProducts(searchKeyword, productType);
    }

    /**
     * 상품 ID로 조회
     */
    public ProductDTO getProductById(int productNo) {
        return productMapper.selectProductById(productNo);
    }

    /**
     * 상품 추가
     */
    public boolean createProduct(ProductDTO productDTO) {
        try {
            int result = productMapper.insertProduct(productDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("상품 추가 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 상품 수정
     */
    public boolean updateProduct(ProductDTO productDTO) {
        try {
            int result = productMapper.updateProduct(productDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("상품 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 상품 삭제 (soft delete)
     */
    public boolean deleteProduct(int productNo) {
        try {
            int result = productMapper.deleteProduct(productNo);
            return result > 0;
        } catch (Exception e) {
            log.error("상품 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 상품명 중복 체크
     */
    public boolean isProductNameDuplicate(String productName) {
        int count = productMapper.countByProductName(productName);
        return count > 0;
    }

    /**
     * 키워드 검색
     */
    public List<ProductDTO> searchProducts(String keyword) {

        return productMapper.searchProducts(keyword);
    }

    /**
     * ★★★ 카테고리별 상품 조회 추가 + 상품가입타입 null 오류 수정(인터넷, 영업점, 스마트폰가입) ★★★
     */
    // ProductService.java
    public List<ProductDTO> getProductsByCategory(int categoryId) {
        log.info("카테고리별 상품 조회 - categoryId: {}", categoryId);

        List<ProductDTO> productList = productMapper.selectProductsByCategory(categoryId);

        // ★★★ 여기 반복문이 누락되어 오류가 발생. ★★★
        for (ProductDTO product : productList) {
            // DTO의 joinTypesStr (DB 문자열)을 joinTypes (List)로 변환하고 null 처리
            if (product.getJoinTypesStr() != null && !product.getJoinTypesStr().isEmpty()) {
                product.setJoinTypes(Arrays.asList(product.getJoinTypesStr().split(",")));
            } else {
                // joinTypes 필드를 null 대신 빈 리스트로 초기화하여 Thymeleaf 오류 방지
                product.setJoinTypes(new ArrayList<>());
            }
        }

        return productList;
    }

}
