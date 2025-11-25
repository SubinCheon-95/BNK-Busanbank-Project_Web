package kr.co.busanbank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class GPTAnalysisService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public GPTAnalysisService(@Value("${app.openai.api-key:}") String openaiApiKey) {

        System.out.println("🔥 Loaded OpenAI Key = " + openaiApiKey);

        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            // 키 없으면 GPT 사용 안함 → 규칙 기반 분석만 사용
            this.webClient = null;
        } else {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")   // ★ 절대 변경 금지
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
    }

    /**
     * GPT 분석 (요약/키워드/감성/추천상품)
     */
    public Optional<Map<String,Object>> analyzeWithGPT(String title, String body) {
        if (webClient == null) return Optional.empty(); // GPT 사용 안함

        try {
            // SYSTEM 역할
            String systemMsg =
                    "당신은 한국어 뉴스 분석을 수행하는 비서입니다. " +
                            "입력된 뉴스 제목과 본문을 JSON 형식으로 분석해서 반환하세요. " +
                            "반드시 이 JSON 형식만 출력하세요: " +
                            "{\"summary\":\"...\",\"keywords\":[\"k1\",\"k2\"],\"sentiment\":{\"label\":\"긍정/부정/중립\",\"score\":0.0},\"recommendations\":[{\"productName\":\"\",\"maturityRate\":0.0,\"description\":\"\"}]}";

            // USER 프롬프트
            String userPrompt =
                    "제목: " + (title == null ? "" : title) +
                            "\n본문:\n" + (body == null ? "" : body) +
                            "\n위 규칙에 맞춰 JSON만 출력하세요.";

            // GPT 요청 Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "gpt-4o-mini"); // ★ 최신, 가장 안정적
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", systemMsg),
                    Map.of("role", "user", "content", userPrompt)
            ));
            payload.put("max_tokens", 800);
            payload.put("temperature", 0.2);

            // GPT API 호출
            String response = webClient.post()
                    .uri("/chat/completions")     // ★ 핵심 수정: 절대 건드리지 마!
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));

            if (response == null) return Optional.empty();

            // JSON 파싱
            JsonNode root = mapper.readTree(response);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode()) return Optional.empty();

            String contentStr = content.asText().trim();
            contentStr = contentStr
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("\\s*```$", "");

            JsonNode parsed = mapper.readTree(contentStr);
            Map<String, Object> out = mapper.convertValue(parsed, Map.class);

            return Optional.of(out);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
