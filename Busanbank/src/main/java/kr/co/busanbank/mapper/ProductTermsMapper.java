package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ProductTermsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** ******************************************
 *             ProductTermsMapper
 ******************************************** */
@Mapper
public interface ProductTermsMapper {

    /**
     * 상품별 약관 목록 조회
     */
    List<ProductTermsDTO> selectTermsByProductNo(@Param("productNo") int productNo);

    /**
     * 약관 ID로 조회
     */
    ProductTermsDTO selectTermById(@Param("termId") int termId);

    /**
     * 필수 약관 개수 조회
     */
    int countRequiredTerms(@Param("productNo") int productNo);
}