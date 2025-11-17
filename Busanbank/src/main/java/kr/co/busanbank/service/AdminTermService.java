package kr.co.busanbank.service;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.mapper.TermMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-17
 * 설명: 정책약관 관리 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminTermService {

    private final TermMapper termMapper;

    /**
     * 약관 목록 조회 (페이징)
     */
    public List<TermDTO> getTermList(int page, int size, String searchKeyword, String termType) {
        int offset = (page - 1) * size;
        return termMapper.selectTermList(offset, size, searchKeyword, termType);
    }

    /**
     * 약관 전체 개수
     */
    public int getTotalCount(String searchKeyword, String termType) {
        return termMapper.countTerms(searchKeyword, termType);
    }

    /**
     * 약관 ID로 조회
     */
    public TermDTO getTermById(int termNo) {
        return termMapper.selectTermById(termNo);
    }

    /**
     * 약관 추가
     */
    public boolean createTerm(TermDTO termDTO) {
        try {
            int result = termMapper.insertTerm(termDTO);
            log.info("약관 추가 성공: {}", termDTO.getTermTitle());
            return result > 0;
        } catch (Exception e) {
            log.error("약관 추가 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 약관 수정
     */
    public boolean updateTerm(TermDTO termDTO) {
        try {
            int result = termMapper.updateTerm(termDTO);
            log.info("약관 수정 성공: {}", termDTO.getTermTitle());
            return result > 0;
        } catch (Exception e) {
            log.error("약관 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 약관 삭제 (soft delete)
     */
    public boolean deleteTerm(int termNo) {
        try {
            int result = termMapper.deleteTerm(termNo);
            log.info("약관 삭제 성공: termNo={}", termNo);
            return result > 0;
        } catch (Exception e) {
            log.error("약관 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 약관 제목 중복 체크
     */
    public boolean isTermTitleDuplicate(String termTitle) {
        int count = termMapper.countByTermTitle(termTitle);
        return count > 0;
    }

    /**
     * 최신 버전 약관 조회 (특정 타입)
     */
    public TermDTO getLatestTermByType(String termType) {
        return termMapper.selectLatestTermByType(termType);
    }

    /**
     * 전체 약관 목록 조회
     */
    public List<TermDTO> getAllTerms() {
        return termMapper.selectAllTerms();
    }
}
