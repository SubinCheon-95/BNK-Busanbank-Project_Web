package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.AttendanceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-12-16
 * 설명: 출석체크 Mapper
 */
@Mapper
public interface AttendanceMapper {

    /**
     * 오늘 출석 여부 확인
     */
    int countTodayAttendance(@Param("userId") Integer userId);

    /**
     * 최근 출석 일자 조회 (연속 출석 계산용)
     */
    AttendanceDTO selectLatestAttendance(@Param("userId") Integer userId);

    /**
     * 출석 등록
     */
    int insertAttendance(AttendanceDTO attendance);

    /**
     * 사용자별 출석 히스토리 조회
     */
    List<AttendanceDTO> selectAttendancesByUserId(@Param("userId") Integer userId);

    /**
     * 사용자 총 출석일수 조회
     */
    int countTotalAttendance(@Param("userId") Integer userId);

    /**
     * 사용자 총 획득 포인트 조회
     */
    Integer selectTotalEarnedPoints(@Param("userId") Integer userId);

    /**
     * 이번 주 출석 현황 조회 (월~일)
     */
    List<AttendanceDTO> selectWeeklyAttendance(@Param("userId") Integer userId);
}
