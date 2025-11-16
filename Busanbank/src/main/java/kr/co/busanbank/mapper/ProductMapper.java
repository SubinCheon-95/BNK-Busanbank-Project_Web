package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 전체 상품 조회
    List<ProductDTO> findAll();

    // 상품 상세조회
    ProductDTO findById(@Param("productNo") int productNo);

    // 키워드 검색
    List<ProductDTO> searchProducts(@Param("keyword") String keyword);

}