package kr.co.busanbank.service;

import kr.co.busanbank.dto.AttendanceDTO;
import kr.co.busanbank.dto.AttendanceRewardDTO;
import kr.co.busanbank.mapper.AttendanceMapper;
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
    private final AttendanceMapper attendanceMapper;
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

            // 연속 출석 일수 계산 (2025-12-28 수정 - 작성자: 진원)
            if (latestAttendance != null) {
                // 오늘 날짜 (00:00:00)
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(today);
                todayCal.set(Calendar.HOUR_OF_DAY, 0);
                todayCal.set(Calendar.MINUTE, 0);
                todayCal.set(Calendar.SECOND, 0);
                todayCal.set(Calendar.MILLISECOND, 0);

                // 마지막 출석 날짜 (00:00:00)
                Calendar latestCal = Calendar.getInstance();
                latestCal.setTime(latestAttendance.getAttendanceDate());
                latestCal.set(Calendar.HOUR_OF_DAY, 0);
                latestCal.set(Calendar.MINUTE, 0);
                latestCal.set(Calendar.SECOND, 0);
                latestCal.set(Calendar.MILLISECOND, 0);

                // 날짜 차이 계산 (일 단위)
                long diffInMillis = todayCal.getTimeInMillis() - latestCal.getTimeInMillis();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                // 어제 출석한 경우(1일 차이) 연속 일수 증가
                if (diffInDays == 1) {
                    consecutiveDays = latestAttendance.getConsecutiveDays() + 1;
                }
                // 그 외의 경우 연속 끊김, consecutiveDays = 1 유지
            }

            // 포인트 계산: 연속일수 * 10 (2025-12-28 수정 - 작성자: 진원)
            int earnedPoints = consecutiveDays * 10;

            // 출석 기록 저장
            AttendanceDTO attendance = AttendanceDTO.builder()
                    .userId(userId)
                    .attendanceDate(today)
                    .consecutiveDays(consecutiveDays)
                    .earnedPoints(earnedPoints)
                    .build();
            pointMapper.insertAttendance(attendance);

            // 포인트 지급
            String description = "출석체크 " + consecutiveDays + "일차 (" + consecutiveDays + " x 10P)";
            pointService.earnPoints(userId, earnedPoints, description);

            result.put("success", true);
            result.put("consecutiveDays", consecutiveDays);
            result.put("earnedPoints", earnedPoints);
            String message = "출석체크 완료! " + earnedPoints + "P 획득 (연속 " + consecutiveDays + "일)";
            result.put("message", message);

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

    /**
     * 이번 주 출석 현황 조회 (월~일)
     * 2025-12-28 - 주간 출석 현황 조회 기능 추가 - 작성자: 진원
     */
    public boolean[] getWeeklyAttendance(int userId) {
        // boolean[7] 배열 초기화 (월~일, 인덱스 0=월요일, 6=일요일)
        boolean[] result = new boolean[7];

        try {
            // DB에서 이번 주 출석 기록 조회 (월요일 ~ 일요일)
            List<AttendanceDTO> weeklyData = attendanceMapper.selectWeeklyAttendance(userId);

            if (weeklyData == null || weeklyData.isEmpty()) {
                return result; // 출석 기록 없으면 모두 false
            }

            // 각 출석 기록을 요일별로 매핑
            for (AttendanceDTO attendance : weeklyData) {
                Calendar attendanceDate = Calendar.getInstance();
                attendanceDate.setTime(attendance.getAttendanceDate());

                // Calendar.DAY_OF_WEEK: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
                int dayOfWeek = attendanceDate.get(Calendar.DAY_OF_WEEK);

                // 배열 인덱스로 변환 (월=0, 화=1, ..., 일=6)
                int dayIndex;
                if (dayOfWeek == Calendar.SUNDAY) {
                    dayIndex = 6; // 일요일은 마지막 (인덱스 6)
                } else {
                    dayIndex = dayOfWeek - Calendar.MONDAY; // 월=0, 화=1, 수=2, ..., 토=5
                }

                if (dayIndex >= 0 && dayIndex < 7) {
                    result[dayIndex] = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
