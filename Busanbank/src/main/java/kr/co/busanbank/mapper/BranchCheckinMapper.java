package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.BranchCheckinDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-30
 * 설명: 영업점 체크인 Mapper
 */
@Mapper
public interface BranchCheckinMapper {

    /**
     * 오늘 체크인 여부 확인
     */
    int countTodayCheckin(@Param("userId") int userId);

    /**
     * 체크인 등록
     */
    int insertCheckin(BranchCheckinDTO checkin);

    /**
     * 사용자별 체크인 히스토리 조회
     */
    List<BranchCheckinDTO> selectCheckinsByUserId(@Param("userId") int userId);

    /**
     * 영업점별 체크인 횟수 조회
     */
    int countCheckinsByBranch(@Param("branchId") Integer branchId);

    /**
     * 최근 체크인 내역 조회 (limit 적용)
     */
    List<BranchCheckinDTO> selectRecentCheckins(@Param("userId") int userId, @Param("limit") int limit);
}
