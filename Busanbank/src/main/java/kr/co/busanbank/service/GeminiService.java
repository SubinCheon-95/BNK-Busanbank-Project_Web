/*
    날짜 : 2025/11/26
    이름 : 오서정
    내용 : 제미나이 서비스 작성
 */

package kr.co.busanbank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${spring.gemini.api.key}")
    private String apiKey;

    public String askGemini(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> contentItem = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(contentItem));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = new RestTemplate().postForEntity(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
                entity,
                Map.class
        );

        Map<String, Object> resBody = res.getBody();
        if (resBody == null || !resBody.containsKey("candidates")) return "(응답 없음)";

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) resBody.get("candidates");
        if (candidates.isEmpty()) return "(응답 없음)";

        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts.isEmpty()) return "(응답 없음)";

        Map<String, Object> firstPart = parts.get(0);
        return (String) firstPart.get("text");
    }
}
