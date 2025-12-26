package kr.co.busanbank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
    }

    /**
     * ================================
     *   GPT 분석 (요약/키워드/감성/도메인 분석)
     * ================================
     */
    public Optional<Map<String,Object>> analyzeWithGPT(String title, String body) {
        if (webClient == null) return Optional.empty(); // GPT 사용 안함

        try {

            // ---------------------------------------------------------
            // 1) SYSTEM PROMPT (✅ matchedWords 추가!)
            // ---------------------------------------------------------
            String systemMsg = """
                    당신은 뉴스 분석 전문가입니다.
                    다음 입력된 뉴스(제목 + 본문)를 기반으로 고품질 분석을 수행하세요.

                    ★ 요약 규칙 
                      1) 핵심 사실, 통계, 배경, 원인·결과를 포함한 **4~7문장 요약**
                      2) 기사 맥락 유지
                      3) 뉴스 핵심 키워드 5개 추출
                      4) 감성 분석(긍정/부정/중립 + 점수)
                      5) 감성 분석 시 매칭된 긍정/부정 단어 리스트도 추출
                      6) 관련 금융상품 추천

                    ★ 출력 형식(JSON)
                    {
                      "summary": "...",
                      "keywords": ["...", "..."],
                      "sentiment": {
                        "label": "긍정 | 부정 | 중립",
                        "score": 0.00,
                        "matchedPositiveWords": ["급등", "호조", "성장"],
                        "matchedNegativeWords": ["폭락", "손실", "하락"]
                      },
                      "domainKeywords": ["...", "..."]
                    }
                    
                    ** 감성 분석 단어 추출 규칙:
                    - 긍정 단어: 상승, 급등, 호조, 개선, 성장, 회복, 이익, 증가 등
                    - 부정 단어: 하락, 폭락, 손실, 위기, 불안, 침체, 감소 등
                    - 기사에서 실제로 등장한 단어만 포함
                    - 최대 20개까지만
                    """;

            // ---------------------------------------------------------
            // 2) USER PROMPT
            // ---------------------------------------------------------
            String userPrompt = """
                    제목: %s

                    본문:
                    %s

                    위 요구사항을 준수하여 JSON만 출력하세요.
                    """.formatted(
                    title == null ? "" : title,
                    body == null ? "" : body
            );

            // ---------------------------------------------------------
            // 3) 요청 Payload
            // ---------------------------------------------------------
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "gpt-4o-mini");
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", systemMsg),
                    Map.of("role", "user", "content", userPrompt)
            ));
            payload.put("max_tokens", 1200);  // ✅ 단어 리스트 추가로 늘림
            payload.put("temperature", 0.2);

            // ---------------------------------------------------------
            // 4) GPT API 호출
            // ---------------------------------------------------------
            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // ---------------------------------------------------------
            // 5) JSON 파싱
            // ---------------------------------------------------------
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                System.err.println("❌ GPT 응답에 choices가 없음");
                return Optional.empty();
            }

            String content = choices.get(0)
                    .path("message")
                    .path("content")
                    .asText("");

            if (content.isBlank()) {
                System.err.println("❌ GPT 응답 내용이 비어있음");
                return Optional.empty();
            }

            // ---------------------------------------------------------
            // 6) GPT가 반환한 JSON 파싱
            // ---------------------------------------------------------
            String cleaned = content
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);

            System.out.println("✅ GPT 분석 완료:");
            System.out.println("   summary: " + result.get("summary"));
            System.out.println("   keywords: " + result.get("keywords"));
            System.out.println("   sentiment: " + result.get("sentiment"));

            return Optional.of(result);

        } catch (Exception e) {
            System.err.println("❌ GPT 분석 실패: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}