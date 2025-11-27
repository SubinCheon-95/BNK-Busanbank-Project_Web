package kr.co.busanbank.service;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 포인트 서비스
 */
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointMapper pointMapper;

    /**
     * 사용자 포인트 정보 조회
     */
    public UserPointDTO getUserPoint(int userId) {
        UserPointDTO userPoint = pointMapper.selectUserPointByUserId(userId);
        if (userPoint == null) {
            // 포인트 정보가 없으면 새로 생성
            userPoint = UserPointDTO.builder()
                    .userId(userId)
                    .totalEarned(0)
                    .currentPoint(0)
                    .totalUsed(0)
                    .userLevel(1)
                    .build();
            pointMapper.insertUserPoint(userPoint);
            userPoint = pointMapper.selectUserPointByUserId(userId);
        }
        return userPoint;
    }

    /**
     * 포인트 획득 처리
     * @param userId 사용자 ID
     * @param amount 포인트 금액
     * @param description 설명
     * @return 성공 여부
     */
    @Transactional
    public boolean earnPoints(int userId, int amount, String description) {
        try {
            // 사용자 포인트 정보가 없으면 생성
            UserPointDTO userPoint = pointMapper.selectUserPointByUserId(userId);

            if (userPoint == null) {
                getUserPoint(userId);
                userPoint = pointMapper.selectUserPointByUserId(userId);
            }

            // 현재 잔액 조회
            Integer currentBalance = userPoint.getCurrentPoint();
            if (currentBalance == null) {
                currentBalance = 0;
            }

            // 포인트 증가
            pointMapper.updateUserPointAfterEarn(userId, amount);

            // pointSource 결정
            String pointSource = "MANUAL";
            if (description != null) {
                if (description.contains("출석")) {
                    pointSource = "ATTENDANCE";
                } else if (description.contains("퀴즈")) {
                    pointSource = "QUIZ";
                }
            }

            // 포인트 히스토리 기록
            PointHistoryDTO history = PointHistoryDTO.builder()
                    .userId(userId)
                    .pointType("EARN")
                    .pointSource(pointSource)
                    .pointAmount(amount)
                    .balanceBefore(currentBalance)
                    .balanceAfter(currentBalance + amount)
                    .description(description)
                    .build();
            pointMapper.insertPointHistory(history);

            // 레벨 업데이트 확인
            updateUserLevel(userId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 포인트 사용 처리
     * @param userId 사용자 ID
     * @param amount 포인트 금액
     * @param description 설명
     * @return 성공 여부
     */
    @Transactional
    public boolean usePoints(int userId, int amount, String description) {
        try {
            // 현재 포인트 확인
            UserPointDTO userPoint = pointMapper.selectUserPointByUserId(userId);
            if (userPoint == null || userPoint.getCurrentPoint() < amount) {
                return false; // 포인트 부족
            }

            Integer currentBalance = userPoint.getCurrentPoint();

            // 포인트 차감
            pointMapper.updateUserPointAfterUse(userId, amount);

            // 포인트 히스토리 기록
            PointHistoryDTO history = PointHistoryDTO.builder()
                    .userId(userId)
                    .pointType("USE")
                    .pointSource("MANUAL")
                    .pointAmount(amount)
                    .balanceBefore(currentBalance)
                    .balanceAfter(currentBalance - amount)
                    .description(description)
                    .build();
            pointMapper.insertPointHistory(history);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자 레벨 업데이트
     */
    @Transactional
    public void updateUserLevel(int userId) {
        UserPointDTO userPoint = pointMapper.selectUserPointByUserId(userId);
        if (userPoint != null) {
            LevelSettingDTO newLevel = pointMapper.selectLevelByTotalPoints(userPoint.getTotalEarned());
            if (newLevel != null && !newLevel.getLevelNumber().equals(userPoint.getUserLevel())) {
                pointMapper.updateUserLevel(userId, newLevel.getLevelNumber());
            }
        }
    }

    /**
     * 포인트 히스토리 조회 (페이징)
     */
    public Map<String, Object> getPointHistory(int userId, int page, int size) {
        int offset = (page - 1) * size;
        List<PointHistoryDTO> historyList = pointMapper.selectPointHistoryByUserId(userId, offset, size);
        int totalCount = pointMapper.countPointHistoryByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String, Object> result = new HashMap<>();
        result.put("historyList", historyList);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);

        return result;
    }

    /**
     * 월별 획득 포인트 합계
     */
    public int getMonthlyEarnedPoints(int userId, int year, int month) {
        Integer points = pointMapper.selectMonthlyEarnedPoints(userId, year, month);
        return points != null ? points : 0;
    }
}
