package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 포인트 시스템 Mapper
 */
@Mapper
public interface PointMapper {

    // ========== UserPoint ==========
    UserPointDTO selectUserPointByUserId(@Param("userId") int userId);
    int insertUserPoint(UserPointDTO userPointDTO);
    int updateUserPoint(UserPointDTO userPointDTO);
    int updateUserPointAfterEarn(@Param("userId") int userId, @Param("amount") int amount);
    int updateUserPointAfterUse(@Param("userId") int userId, @Param("amount") int amount);
    int updateUserLevel(@Param("userId") int userId, @Param("userLevel") int userLevel);

    // ========== PointHistory ==========
    List<PointHistoryDTO> selectPointHistoryByUserId(
            @Param("userId") int userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
    int countPointHistoryByUserId(@Param("userId") int userId);
    int insertPointHistory(PointHistoryDTO pointHistoryDTO);

    // 월별 획득 포인트 합계
    Integer selectMonthlyEarnedPoints(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month
    );

    // 포인트 히스토리 최근 잔액 조회
    Integer selectLatestBalance(@Param("userId") int userId);

    // ========== Attendance ==========
    AttendanceDTO selectTodayAttendance(
            @Param("userId") int userId,
            @Param("today") Date today
    );
    AttendanceDTO selectLatestAttendance(@Param("userId") int userId);
    int insertAttendance(AttendanceDTO attendanceDTO);
    List<AttendanceDTO> selectAttendanceHistory(
            @Param("userId") int userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
    int countAttendanceByUserId(@Param("userId") int userId);

    // ========== AttendanceReward ==========
    List<AttendanceRewardDTO> selectAllAttendanceRewards();
    AttendanceRewardDTO selectAttendanceRewardByDays(@Param("consecutiveDays") int consecutiveDays);
    int insertAttendanceReward(AttendanceRewardDTO attendanceRewardDTO);
    int updateAttendanceReward(AttendanceRewardDTO attendanceRewardDTO);
    int deleteAttendanceReward(@Param("rewardId") int rewardId);

    // ========== LevelSetting ==========
    List<LevelSettingDTO> selectAllLevelSettings();
    LevelSettingDTO selectLevelSettingByNumber(@Param("levelNumber") int levelNumber);
    LevelSettingDTO selectLevelByTotalPoints(@Param("totalPoints") int totalPoints);
    int insertLevelSetting(LevelSettingDTO levelSettingDTO);
    int updateLevelSetting(LevelSettingDTO levelSettingDTO);
    int deleteLevelSetting(@Param("levelId") int levelId);

    // ========== Ranking ==========
    List<RankingDTO> selectRankingAll(@Param("limit") int limit);
    List<RankingDTO> selectRankingMonthly(
            @Param("year") int year,
            @Param("month") int month,
            @Param("limit") int limit
    );
    Integer selectUserRankAll(@Param("userId") int userId);
    Integer selectUserRankMonthly(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month
    );
    /**
     * 사용자 현재 포인트 조회 - 김수진
     */
    Integer selectUserPoints(@Param("userNo") Long userNo);
}
