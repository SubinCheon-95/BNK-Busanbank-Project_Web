package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductTermsDTO;
import kr.co.busanbank.mapper.ProductTermsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** ************************************************
 *               ProductTermsService
 ************************************************** */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductTermsService {

    private final ProductTermsMapper productTermsMapper;

    /**
     * 상품별 약관 목록 조회
     */
    public List<ProductTermsDTO> getTermsByProductNo(int productNo) {
        log.info("상품 약관 조회 - productNo: {}", productNo);
        return productTermsMapper.selectTermsByProductNo(productNo);
    }

    /**
     * 약관 ID로 조회
     */
    public ProductTermsDTO getTermById(int termId) {
        return productTermsMapper.selectTermById(termId);
    }

    /**
     * 필수 약관 동의 검증
     * @param productNo 상품번호
     * @param agreedTermIds 동의한 약관 ID 목록
     * @return 모든 필수 약관에 동의했는지 여부
     */
    public boolean validateRequiredTerms(int productNo, List<Integer> agreedTermIds) {
        log.info("필수 약관 검증 - productNo: {}, agreedTermIds: {}", productNo, agreedTermIds);

        if (agreedTermIds == null || agreedTermIds.isEmpty()) {
            return false;
        }

        // 필수 약관 목록 조회
        List<ProductTermsDTO> allTerms = productTermsMapper.selectTermsByProductNo(productNo);
        List<ProductTermsDTO> requiredTerms = allTerms.stream()
                .filter(term -> "Y".equals(term.getIsRequired()))
                .toList();

        // 모든 필수 약관이 동의 목록에 포함되어 있는지 확인
        for (ProductTermsDTO required : requiredTerms) {
            if (!agreedTermIds.contains(required.getTermId())) {
                log.warn("필수 약관 미동의 - termId: {}", required.getTermId());
                return false;
            }
        }

        return true;
    }

    /**
     * 필수 약관 개수 조회
     */
    public int countRequiredTerms(int productNo) {
        return productTermsMapper.countRequiredTerms(productNo);
    }
}