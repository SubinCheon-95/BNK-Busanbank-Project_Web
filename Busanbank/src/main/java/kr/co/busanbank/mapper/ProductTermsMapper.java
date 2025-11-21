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

    /**
     * 상품약관 추가
     */
    int insertProductTerm(ProductTermsDTO productTermsDTO);

    /**
     * 상품약관 수정
     */
    int updateProductTerm(ProductTermsDTO productTermsDTO);

    /**
     * 상품약관 삭제 (상태 변경)
     */
    int deleteProductTerm(@Param("termId") int termId);

    /**
     * 특정 상품의 모든 약관 조회 (관리자용 - 상태 무관)
     */
    List<ProductTermsDTO> selectAllTermsByProductNo(@Param("productNo") int productNo);

    /**
     * 표시 순서 최대값 조회
     */
    int selectMaxDisplayOrder(@Param("productNo") int productNo);
}