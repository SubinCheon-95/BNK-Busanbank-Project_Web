package kr.co.busanbank.service;

import jakarta.transaction.Transactional;
import kr.co.busanbank.dto.PointHistoryDTO;
import kr.co.busanbank.dto.UserPointDTO;
import kr.co.busanbank.mapper.CameraMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
*  작성자: 윤종인
*  작성일: 2025-12-19
*  설명: 이미지 일치시 포인트 지급
* */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CameraService {
    private final CameraMapper cameraMapper;

    public Map<String, Object> checkImage(int userId) {
        int count = cameraMapper.countTodayReward(userId);
        log.info("📊 [CameraService] 오늘 보상 count={}", count);
        if (count > 0) {
            return Map.of(
                    "success", false,
                    "message", "오늘은 이미 카메라 보상을 받았습니다."
            );
        }

        int point = 100;

        // 2️⃣ CAMERACHECK insert
        int cameraInserted = cameraMapper.insertCameraReward(userId, point);
        log.info("📝 [CameraService] CAMERACHECK insert rows={}", cameraInserted);

        // 3️⃣ USERPOINT update
        int pointUpdated = cameraMapper.updateUserPointAfterEarn(userId, point);
        log.info("💰 [CameraService] USERPOINT update rows={}", pointUpdated);

        UserPointDTO userPoint = cameraMapper.selectUserPointByUserId(userId);
        Integer currentBalance = userPoint.getCurrentPoint();

        // 4️⃣ POINTHISTORY insert
        int historyInserted = cameraMapper.insertPointHistory(
                PointHistoryDTO.builder()
                        .userId(userId)
                        .pointType("EARN")
                        .pointSource("CAMERA")
                        .pointAmount(point)
                        .balanceBefore(currentBalance)
                        .balanceAfter(currentBalance - point)
                        .description("TV 이미지 인식 보상")
                        .build()
        );
        log.info("📜 [CameraService] POINTHISTORY insert rows={}", historyInserted);

        log.info("🎉 [CameraService] 카메라 보상 처리 완료");
        return Map.of(
                "success", true,
                "point", point
        );
    }
}
