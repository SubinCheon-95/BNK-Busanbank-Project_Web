package kr.co.busanbank.service;

import kr.co.busanbank.dto.AttendanceDTO;
import kr.co.busanbank.dto.AttendanceRewardDTO;
import kr.co.busanbank.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 출석체크 서비스
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final PointMapper pointMapper;
    private final PointService pointService;

    /**
     * 출석체크 처리
     * @param userId 사용자 ID
     * @return 출석 정보 (연속일수, 획득 포인트)
     */
    @Transactional
    public Map<String, Object> checkAttendance(int userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Date today = new Date();

            // 오늘 이미 출석했는지 확인
            AttendanceDTO todayAttendance = pointMapper.selectTodayAttendance(userId, today);
            if (todayAttendance != null) {
                result.put("success", false);
                result.put("message", "오늘은 이미 출석체크를 완료했습니다.");
                return result;
            }

            // 최근 출석 기록 조회
            AttendanceDTO latestAttendance = pointMapper.selectLatestAttendance(userId);

            int consecutiveDays = 1;

            // 연속 출석 일수 계산
            if (latestAttendance != null) {
                Calendar latest = Calendar.getInstance();
                latest.setTime(latestAttendance.getAttendanceDate());
                latest.set(Calendar.HOUR_OF_DAY, 0);
                latest.set(Calendar.MINUTE, 0);
                latest.set(Calendar.SECOND, 0);
                latest.set(Calendar.MILLISECOND, 0);

                Calendar yesterday = Calendar.getInstance();
                yesterday.setTime(today);
                yesterday.add(Calendar.DATE, -1);
                yesterday.set(Calendar.HOUR_OF_DAY, 0);
                yesterday.set(Calendar.MINUTE, 0);
                yesterday.set(Calendar.SECOND, 0);
                yesterday.set(Calendar.MILLISECOND, 0);

                // 어제 출석한 경우 연속 일수 증가
                if (latest.equals(yesterday)) {
                    consecutiveDays = latestAttendance.getConsecutiveDays() + 1;
                }
            }

            // 보상 포인트 계산
            AttendanceRewardDTO reward = pointMapper.selectAttendanceRewardByDays(consecutiveDays);
            int earnedPoints = (reward != null) ? reward.getRewardPoints() : 10; // 기본 10포인트

            // 출석 기록 저장
            AttendanceDTO attendance = AttendanceDTO.builder()
                    .userId(userId)
                    .attendanceDate(today)
                    .consecutiveDays(consecutiveDays)
                    .earnedPoints(earnedPoints)
                    .build();
            pointMapper.insertAttendance(attendance);

            // 포인트 지급
            pointService.earnPoints(userId, earnedPoints, "출석체크 " + consecutiveDays + "일차");

            result.put("success", true);
            result.put("consecutiveDays", consecutiveDays);
            result.put("earnedPoints", earnedPoints);
            result.put("message", "출석체크 완료! " + earnedPoints + " 포인트를 획득했습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "출석체크 처리 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * 출석 이력 조회 (페이징)
     */
    public Map<String, Object> getAttendanceHistory(int userId, int page, int size) {
        int offset = (page - 1) * size;
        List<AttendanceDTO> attendanceList = pointMapper.selectAttendanceHistory(userId, offset, size);
        int totalCount = pointMapper.countAttendanceByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String, Object> result = new HashMap<>();
        result.put("attendanceList", attendanceList);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);

        return result;
    }

    /**
     * 오늘 출석했는지 확인
     */
    public boolean isAttendedToday(int userId) {
        Date today = new Date();
        AttendanceDTO todayAttendance = pointMapper.selectTodayAttendance(userId, today);
        return todayAttendance != null;
    }

    /**
     * 현재 연속 출석 일수 조회
     */
    public int getCurrentConsecutiveDays(int userId) {
        AttendanceDTO latestAttendance = pointMapper.selectLatestAttendance(userId);
        if (latestAttendance == null) {
            return 0;
        }

        // 어제 또는 오늘 출석했는지 확인
        Calendar latest = Calendar.getInstance();
        latest.setTime(latestAttendance.getAttendanceDate());
        latest.set(Calendar.HOUR_OF_DAY, 0);
        latest.set(Calendar.MINUTE, 0);
        latest.set(Calendar.SECOND, 0);
        latest.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);

        if (latest.equals(today) || latest.equals(yesterday)) {
            return latestAttendance.getConsecutiveDays();
        }

        return 0; // 연속 출석이 끊김
    }

    /**
     * 출석 보상 설정 목록 조회
     */
    public List<AttendanceRewardDTO> getAllRewards() {
        return pointMapper.selectAllAttendanceRewards();
    }

    /**
     * 출석 보상 설정 추가 (관리자)
     */
    @Transactional
    public boolean addReward(AttendanceRewardDTO rewardDTO) {
        try {
            pointMapper.insertAttendanceReward(rewardDTO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 출석 보상 설정 수정 (관리자)
     */
    @Transactional
    public boolean updateReward(AttendanceRewardDTO rewardDTO) {
        try {
            pointMapper.updateAttendanceReward(rewardDTO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 출석 보상 설정 삭제 (관리자)
     */
    @Transactional
    public boolean deleteReward(int rewardId) {
        try {
            pointMapper.deleteAttendanceReward(rewardId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
