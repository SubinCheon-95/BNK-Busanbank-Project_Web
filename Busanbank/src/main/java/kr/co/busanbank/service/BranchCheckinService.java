package kr.co.busanbank.service;

import kr.co.busanbank.dto.BranchCheckinDTO;
import kr.co.busanbank.dto.BranchDTO;
import kr.co.busanbank.helper.GeoUtils;
import kr.co.busanbank.mapper.BranchCheckinMapper;
import kr.co.busanbank.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-30
 * 설명: 영업점 체크인 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BranchCheckinService {

    private final BranchCheckinMapper branchCheckinMapper;
    private final BranchMapper branchMapper;
    private final PointService pointService;

    private static final double CHECKIN_RADIUS_METERS = 100.0; // 체크인 가능 반경 100m
    private static final int CHECKIN_REWARD_POINTS = 100; // 체크인 시 지급 포인트

    /**
     * 체크인 가능 여부 확인
     */
    public boolean canCheckinToday(int userId) {
        log.info("오늘 체크인 가능 여부 확인 - userId: {}", userId);
        int count = branchCheckinMapper.countTodayCheckin(userId);
        return count == 0;
    }

    /**
     * 체크인 처리
     */
    @Transactional
    public String processCheckin(int userId, Integer branchId, Double userLat, Double userLon) {
        log.info("체크인 처리 시작 - userId: {}, branchId: {}, userLat: {}, userLon: {}",
                userId, branchId, userLat, userLon);

        // 1. 오늘 이미 체크인 했는지 확인
        if (!canCheckinToday(userId)) {
            log.warn("오늘 이미 체크인 완료 - userId: {}", userId);
            return "오늘은 이미 체크인을 완료했습니다.";
        }

        // 2. 영업점 정보 조회
        BranchDTO branch = branchMapper.selectBranchById(branchId);
        if (branch == null) {
            log.error("영업점을 찾을 수 없음 - branchId: {}", branchId);
            return "영업점 정보를 찾을 수 없습니다.";
        }

        // 3. 영업점 좌표 확인
        if (branch.getLatitude() == null || branch.getLongitude() == null) {
            log.error("영업점 좌표 정보 없음 - branchId: {}", branchId);
            return "영업점 위치 정보가 등록되지 않았습니다.";
        }

        // 4. 거리 계산 및 검증
        double distance = GeoUtils.calculateDistance(
                userLat, userLon,
                branch.getLatitude(), branch.getLongitude()
        );

        log.info("영업점과의 거리: {}m", distance);

        if (distance > CHECKIN_RADIUS_METERS) {
            log.warn("체크인 반경 초과 - distance: {}m, 허용 반경: {}m", distance, CHECKIN_RADIUS_METERS);
            return String.format("영업점에서 너무 멉니다. (현재 거리: %.0fm, 필요 거리: %.0fm 이내)",
                    distance, CHECKIN_RADIUS_METERS);
        }

        // 5. 체크인 기록 저장
        BranchCheckinDTO checkin = BranchCheckinDTO.builder()
                .userId(userId)
                .branchId(branchId)
                .latitude(userLat)
                .longitude(userLon)
                .pointsReceived(CHECKIN_REWARD_POINTS)
                .build();

        int result = branchCheckinMapper.insertCheckin(checkin);

        if (result > 0) {
            // 6. 포인트 지급
            boolean pointResult = pointService.earnPoints(
                    userId,
                    CHECKIN_REWARD_POINTS,
                    String.format("영업점 방문 체크인 - %s", branch.getBranchName())
            );

            if (pointResult) {
                log.info("체크인 성공 - userId: {}, branchId: {}, 지급 포인트: {}",
                        userId, branchId, CHECKIN_REWARD_POINTS);
                return "SUCCESS";
            } else {
                log.error("포인트 지급 실패 - userId: {}, branchId: {}", userId, branchId);
                return "체크인은 완료되었으나 포인트 지급에 실패했습니다.";
            }
        } else {
            log.error("체크인 기록 저장 실패 - userId: {}, branchId: {}", userId, branchId);
            return "체크인 처리 중 오류가 발생했습니다.";
        }
    }

    /**
     * 사용자별 체크인 히스토리 조회
     */
    public List<BranchCheckinDTO> getCheckinHistory(int userId) {
        log.info("체크인 히스토리 조회 - userId: {}", userId);
        return branchCheckinMapper.selectCheckinsByUserId(userId);
    }

    /**
     * 최근 체크인 내역 조회
     */
    public List<BranchCheckinDTO> getRecentCheckins(int userId, int limit) {
        log.info("최근 체크인 내역 조회 - userId: {}, limit: {}", userId, limit);
        return branchCheckinMapper.selectRecentCheckins(userId, limit);
    }

    /**
     * 영업점별 체크인 횟수 조회
     */
    public int getCheckinCountByBranch(Integer branchId) {
        log.info("영업점별 체크인 횟수 조회 - branchId: {}", branchId);
        return branchCheckinMapper.countCheckinsByBranch(branchId);
    }
}
