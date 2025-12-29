package kr.co.busanbank.service;

// 2025/12/28 - Google Cloud Vision API를 사용한 감정 분석 서비스 - 작성자: 진원
// 2025/12/29 - API 키 방식으로 변경 - 작성자: 진원

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class EmotionAnalysisService {

    @Value("${spring.google.vision.api-key}")
    private String visionApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Google Cloud Vision API로 얼굴 감정 분석 (REST API 사용)
     */
    public Map<String, Object> analyzeFaceEmotion(MultipartFile imageFile) throws IOException {
        log.info("🎭 [감정 분석] 시작 - 파일명: {}", imageFile.getOriginalFilename());

        try {
            // 이미지를 Base64로 인코딩
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

            // 요청 본문 생성
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> requests = new ArrayList<>();

            Map<String, Object> request = new HashMap<>();

            // 이미지 설정
            Map<String, String> image = new HashMap<>();
            image.put("content", base64Image);
            request.put("image", image);

            // 기능 설정 (얼굴 감지)
            List<Map<String, String>> features = new ArrayList<>();
            Map<String, String> feature = new HashMap<>();
            feature.put("type", "FACE_DETECTION");
            feature.put("maxResults", "1");
            features.add(feature);
            request.put("features", features);

            requests.add(request);
            requestBody.put("requests", requests);

            // API 호출
            String url = "https://vision.googleapis.com/v1/images:annotate?key=" + visionApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String responseJson = restTemplate.postForObject(url, entity, String.class);
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);

            // 응답 파싱
            Map<String, Object> result = new HashMap<>();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> responses = (List<Map<String, Object>>) responseMap.get("responses");

            if (responses == null || responses.isEmpty()) {
                log.warn("⚠️ [감정 분석] 응답이 비어있음");
                result.put("success", false);
                result.put("message", "얼굴이 감지되지 않았습니다");
                return result;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> firstResponse = responses.get(0);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> faceAnnotations =
                (List<Map<String, Object>>) firstResponse.get("faceAnnotations");

            if (faceAnnotations == null || faceAnnotations.isEmpty()) {
                log.warn("⚠️ [감정 분석] 얼굴이 감지되지 않음");
                result.put("success", false);
                result.put("message", "얼굴이 감지되지 않았습니다");
                return result;
            }

            // 첫 번째 얼굴의 감정 분석 결과
            Map<String, Object> face = faceAnnotations.get(0);

            // 감정 확률 추출
            Map<String, String> emotions = new HashMap<>();
            emotions.put("joy", (String) face.get("joyLikelihood"));
            emotions.put("sorrow", (String) face.get("sorrowLikelihood"));
            emotions.put("anger", (String) face.get("angerLikelihood"));
            emotions.put("surprise", (String) face.get("surpriseLikelihood"));

            String joyLevel = (String) face.get("joyLikelihood");

            // 행복 지수 계산 (0-100점)
            int happinessScore = calculateHappinessScore(joyLevel);

            log.info("✅ [감정 분석] 완료 - Joy: {}, Happiness Score: {}", joyLevel, happinessScore);

            result.put("success", true);
            result.put("emotions", emotions);
            result.put("happinessScore", happinessScore);
            result.put("joyLevel", joyLevel);

            return result;

        } catch (Exception e) {
            log.error("❌ [감정 분석] 실패", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "감정 분석 실패: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * Joy 감정 수준을 0-100점 행복 지수로 변환
     */
    private int calculateHappinessScore(String joyLevel) {
        if (joyLevel == null) return 0;

        return switch (joyLevel) {
            case "VERY_LIKELY" -> 100;
            case "LIKELY" -> 80;
            case "POSSIBLE" -> 50;
            case "UNLIKELY" -> 30;
            case "VERY_UNLIKELY" -> 10;
            default -> 0;
        };
    }

    /**
     * 게임별 보상 포인트 계산
     */
    public Map<String, Object> calculateReward(String gameType, Map<String, Object> analysisResult, String targetEmotion) {
        Map<String, Object> reward = new HashMap<>();

        if (!(boolean) analysisResult.get("success")) {
            reward.put("success", false);
            reward.put("points", 0);
            reward.put("message", "얼굴이 감지되지 않았습니다");
            return reward;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> emotions = (Map<String, String>) analysisResult.get("emotions");
        int happinessScore = (int) analysisResult.get("happinessScore");
        String joyLevel = (String) analysisResult.get("joyLevel");

        switch (gameType) {
            case "SMILE_CHALLENGE":
                // 웃음 챌린지: Joy >= LIKELY 이상이면 50P
                if (isLevelAtLeast(joyLevel, "LIKELY")) {
                    reward.put("success", true);
                    reward.put("points", 50);
                    reward.put("message", "웃음 챌린지 성공!");
                } else {
                    reward.put("success", false);
                    reward.put("points", 0);
                    reward.put("message", "더 활짝 웃어주세요!");
                }
                break;

            case "EMOTION_EXPRESS":
                // 감정 표현 게임: 지정된 감정 >= LIKELY 이상이면 100P
                if (targetEmotion != null && emotions.containsKey(targetEmotion)) {
                    String detectedLevel = emotions.get(targetEmotion);
                    if (isLevelAtLeast(detectedLevel, "LIKELY")) {
                        reward.put("success", true);
                        reward.put("points", 100);
                        reward.put("message", getEmotionName(targetEmotion) + " 표현 성공!");
                    } else {
                        reward.put("success", false);
                        reward.put("points", 0);
                        reward.put("message", getEmotionName(targetEmotion) + " 감정을 더 확실하게 표현해주세요!");
                    }
                } else {
                    reward.put("success", false);
                    reward.put("points", 0);
                    reward.put("message", "목표 감정이 지정되지 않았습니다");
                }
                break;

            case "HAPPINESS_METER":
                // 행복 지수 측정: 행복 점수 그대로 포인트 지급 (10~100P)
                int points = happinessScore;
                String message;
                if (happinessScore >= 90) {
                    message = "최고의 행복 지수입니다! +" + points + "P";
                } else if (happinessScore >= 70) {
                    message = "좋은 행복 지수예요! +" + points + "P";
                } else if (happinessScore >= 50) {
                    message = "괜찮은 행복 지수입니다! +" + points + "P";
                } else if (happinessScore >= 30) {
                    message = "조금 더 밝은 표정을 지어보세요! +" + points + "P";
                } else {
                    message = "미소를 지어보세요! +" + points + "P";
                }
                reward.put("success", points > 0);
                reward.put("points", points);
                reward.put("message", message);
                reward.put("happinessScore", happinessScore);
                break;

            default:
                reward.put("success", false);
                reward.put("points", 0);
                reward.put("message", "알 수 없는 게임 타입");
        }

        log.info("🎮 [{}] 결과 - 성공: {}, 포인트: {}",
                gameType, reward.get("success"), reward.get("points"));

        return reward;
    }

    /**
     * 감정 레벨이 기준 이상인지 확인
     */
    private boolean isLevelAtLeast(String actualLevel, String requiredLevel) {
        List<String> levels = List.of("VERY_UNLIKELY", "UNLIKELY", "POSSIBLE", "LIKELY", "VERY_LIKELY");
        int actualIndex = levels.indexOf(actualLevel);
        int requiredIndex = levels.indexOf(requiredLevel);
        return actualIndex >= requiredIndex;
    }

    /**
     * 감정 코드를 한글 이름으로 변환
     */
    private String getEmotionName(String emotion) {
        return switch (emotion) {
            case "joy" -> "기쁨";
            case "sorrow" -> "슬픔";
            case "anger" -> "화남";
            case "surprise" -> "놀람";
            default -> "감정";
        };
    }
}
