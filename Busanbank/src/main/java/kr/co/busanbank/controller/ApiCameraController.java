package kr.co.busanbank.controller;

import kr.co.busanbank.service.CameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
public class ApiCameraController { //이미지 스캔 후 일치시 포인트 지급 - 작성자: 윤종인
    private final CameraService cameraService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkImage(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        log.info("📸 [Camera] /api/camera/check 진입");

        try {
            if (authentication != null && authentication.isAuthenticated()) {
                log.info("🔑 [Flutter] 인증된 사용자: {}", authentication.getName());
            }

            Integer userId = (Integer) request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "userId가 필요합니다."));
            }

            Map<String, Object> result = cameraService.checkImage(userId);

            log.info("✅ [Camera] 처리 결과 = {}", result);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 포인트 지급 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "처리 중 오류가 발생했습니다."));
        }
    }
}
