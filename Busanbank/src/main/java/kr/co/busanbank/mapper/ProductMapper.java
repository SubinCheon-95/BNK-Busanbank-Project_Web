package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 설명: 금융상품 관리 Mapper 인터페이스
 */
@Mapper
public interface ProductMapper {

    // 상품 목록 조회 (페이징)
    List<ProductDTO> selectProductList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("searchKeyword") String searchKeyword,
            @Param("productType") String productType
    );

    // 상품 전체 개수
    int countProducts(
            @Param("searchKeyword") String searchKeyword,
            @Param("productType") String productType
    );

    // 상품 ID로 조회
    ProductDTO selectProductById(@Param("productNo") int productNo);

    // 상품 추가
    int insertProduct(ProductDTO productDTO);

    // 상품 수정
    int updateProduct(ProductDTO productDTO);

    // 상품 삭제 (soft delete)
    int deleteProduct(@Param("productNo") int productNo);

    // 상품명 중복 체크
    int countByProductName(@Param("productName") String productName);

    // 키워드 검색
    List<ProductDTO> searchProducts(@Param("keyword") String keyword);

    // ★★★ 카테고리별 상품 조회 추가 ★★★
    List<ProductDTO> selectProductsByCategory(@Param("categoryId") int categoryId);

    ProductDTO selectProductWithJoinTypes(@Param("productNo") int productNo);

}
