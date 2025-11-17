package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.TermDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-17
 * 설명: 정책약관 관리 Mapper 인터페이스
 */
@Mapper
public interface TermMapper {

    // 약관 목록 조회 (페이징)
    List<TermDTO> selectTermList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("searchKeyword") String searchKeyword,
            @Param("termType") String termType
    );

    // 약관 전체 개수
    int countTerms(
            @Param("searchKeyword") String searchKeyword,
            @Param("termType") String termType
    );

    // 약관 ID로 조회
    TermDTO selectTermById(@Param("termNo") int termNo);

    // 약관 추가
    int insertTerm(TermDTO termDTO);

    // 약관 수정
    int updateTerm(TermDTO termDTO);

    // 약관 삭제 (soft delete)
    int deleteTerm(@Param("termNo") int termNo);

    // 약관 제목 중복 체크
    int countByTermTitle(@Param("termTitle") String termTitle);

    // 최신 버전 약관 조회 (특정 타입)
    TermDTO selectLatestTermByType(@Param("termType") String termType);

    // 전체 약관 목록 조회 (필터 없음)
    List<TermDTO> selectAllTerms();
}
