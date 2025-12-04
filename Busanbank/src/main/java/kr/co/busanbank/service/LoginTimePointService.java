package kr.co.busanbank.service;

import kr.co.busanbank.websocket.PointNotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 작성자: 진원
 * 작성일: 2025-12-04
 * 설명: 로그인 시간 기반 포인트 부여 서비스
 * - 로그인된 사용자에게 30분마다 10포인트 자동 지급
 * - 세션 기반 추적 시스템
 * - WebSocket을 통한 실시간 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginTimePointService {

    private final PointService pointService;
    private final PointNotificationWebSocketHandler pointNotificationHandler;

    // 사용자별 로그인 정보 저장 (userId -> LoginInfo)
    private final Map<Integer, LoginInfo> activeSessions = new ConcurrentHashMap<>();

    // 포인트 부여 주기 (30분 = 1800초)
    private static final long POINT_INTERVAL_SECONDS = 1800;

    // 부여할 포인트
    private static final int POINTS_PER_INTERVAL = 10;

    /**
     * 로그인 시 세션 등록
     */
    public void registerLoginSession(Integer userId) {
        LoginInfo loginInfo = new LoginInfo(userId, LocalDateTime.now());
        activeSessions.put(userId, loginInfo);
        log.info("✅ 로그인 세션 등록 - userId: {}, 로그인 시간: {}", userId, loginInfo.getLoginTime());
    }

    /**
     * 로그아웃 시 세션 제거
     */
    public void removeLoginSession(Integer userId) {
        LoginInfo removed = activeSessions.remove(userId);
        if (removed != null) {
            log.info("❌ 로그인 세션 제거 - userId: {}, 로그인 시간: {}", userId, removed.getLoginTime());
        }
    }

    /**
     * 30분마다 활성 세션 확인 후 포인트 부여
     * 매 30분마다 실행 (초, 분, 시, 일, 월, 요일)
     */
    @Scheduled(cron = "0 0,30 * * * *")
    public void distributeLoginTimePoints() {
        log.info("🎁 로그인 시간 포인트 부여 시작 - 활성 세션: {}개", activeSessions.size());

        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<Integer, LoginInfo> entry : activeSessions.entrySet()) {
            Integer userId = entry.getKey();
            LoginInfo loginInfo = entry.getValue();

            try {
                // 마지막 포인트 부여 시간 확인
                LocalDateTime lastPointTime = loginInfo.getLastPointAwardTime();

                // 30분이 경과했는지 확인
                long secondsSinceLastPoint = java.time.Duration.between(lastPointTime, now).getSeconds();

                if (secondsSinceLastPoint >= POINT_INTERVAL_SECONDS) {
                    // 포인트 부여
                    boolean success = pointService.earnPoints(
                        userId,
                        POINTS_PER_INTERVAL,
                        "로그인 시간 보상 (" + (secondsSinceLastPoint / 60) + "분)"
                    );

                    if (success) {
                        // 마지막 포인트 부여 시간 업데이트
                        loginInfo.updateLastPointAwardTime(now);
                        successCount++;
                        log.info("💰 포인트 부여 성공 - userId: {}, 포인트: {}점, 경과시간: {}분",
                            userId, POINTS_PER_INTERVAL, secondsSinceLastPoint / 60);

                        // WebSocket으로 실시간 알림 전송 (작성자: 진원, 2025-12-04)
                        String notificationMessage = String.format("로그인 시간 보상으로 %d포인트를 받았습니다!", POINTS_PER_INTERVAL);
                        pointNotificationHandler.sendPointNotification(userId, POINTS_PER_INTERVAL, notificationMessage);
                    } else {
                        failCount++;
                        log.warn("⚠️ 포인트 부여 실패 - userId: {}", userId);
                    }
                }
            } catch (Exception e) {
                failCount++;
                log.error("❌ 포인트 부여 중 오류 - userId: {}, 오류: {}", userId, e.getMessage());
            }
        }

        log.info("🎁 로그인 시간 포인트 부여 완료 - 성공: {}건, 실패: {}건, 총: {}건",
            successCount, failCount, activeSessions.size());
    }

    /**
     * 현재 활성 세션 수 조회
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 특정 사용자의 로그인 정보 조회
     */
    public LoginInfo getLoginInfo(Integer userId) {
        return activeSessions.get(userId);
    }

    /**
     * 로그인 정보 내부 클래스
     */
    @lombok.Getter
    public static class LoginInfo {
        private final Integer userId;
        private final LocalDateTime loginTime;
        private LocalDateTime lastPointAwardTime;

        public LoginInfo(Integer userId, LocalDateTime loginTime) {
            this.userId = userId;
            this.loginTime = loginTime;
            this.lastPointAwardTime = loginTime; // 초기값은 로그인 시간
        }

        public void updateLastPointAwardTime(LocalDateTime time) {
            this.lastPointAwardTime = time;
        }
    }
}
