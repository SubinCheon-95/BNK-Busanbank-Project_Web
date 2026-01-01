// 2026/01/02 - 신분증ocr인증 구현 - 작성자: 오서정
package kr.co.busanbank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class VisionOcrService {

    @Value("${spring.gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String detectText(String base64Image) {
        try {
            base64Image = stripDataUrlPrefix(base64Image);
            if (base64Image == null || base64Image.isBlank()) return "";

            String url = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

            Map<String, Object> req = new HashMap<>();
            req.put("image", Map.of("content", base64Image));
            req.put("features", List.of(Map.of("type", "DOCUMENT_TEXT_DETECTION")));

            Map<String, Object> body = Map.of("requests", List.of(req));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("Vision API failed: status={}, body={}", resp.getStatusCode(), resp.getBody());
                return "";
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode first = root.path("responses").isArray() && root.path("responses").size() > 0
                    ? root.path("responses").get(0)
                    : null;

            if (first == null) return "";

            // ✅ 에러 체크
            JsonNode err = first.path("error");
            if (!err.isMissingNode() && err.has("message")) {
                log.warn("Vision API error: {}", err.toString());
                return "";
            }

            // ✅ 1순위: fullTextAnnotation.text
            String full = first.path("fullTextAnnotation").path("text").asText("");
            if (full != null && !full.isBlank()) return full;

            // ✅ 2순위: textAnnotations[0].description
            JsonNode textAnnotations = first.path("textAnnotations");
            if (textAnnotations.isArray() && textAnnotations.size() > 0) {
                String desc = textAnnotations.get(0).path("description").asText("");
                if (desc != null && !desc.isBlank()) return desc;
            }

            return "";
        } catch (Exception e) {
            log.error("Vision OCR error", e);
            return "";
        }
    }

    private String stripDataUrlPrefix(String base64) {
        if (base64 == null) return "";
        int comma = base64.indexOf(',');
        if (base64.startsWith("data:") && comma > 0) {
            return base64.substring(comma + 1);
        }
        return base64;
    }
}
