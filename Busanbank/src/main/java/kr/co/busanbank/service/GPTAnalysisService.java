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
            // 1) SYSTEM PROMPT (강화된 버전)
            // ---------------------------------------------------------
            String systemMsg = """
                    당신은 뉴스 분석 전문가입니다.
                    다음 입력된 뉴스(제목 + 본문)를 기반으로 고품질 분석을 수행하세요.

                    ★ 요약 규칙 
                      1) 핵심 사실, 통계, 배경, 원인·결과를 포함한 **충분히 풍부한 4~7문장 요약**을 생성
                      2) 기사 맥락을 유지하고 불필요한 수식어, 사견 금지
                      3) 뉴스 핵심 키워드 5개 추출
                      4) 감성 분석(긍정/부정/중립 + 점수)
                      5) 뉴스 내용과 관련성 높은 금융 상품을 추천 (가능하면 금리/특징 포함)

                    ★ 키워드 규칙
                    - 5~12개
                    - 한국 금융/경제 맥락의 핵심 개념 중심
                    - 불필요한 일반 용어 제거

                    ★ 감성 분석
                    - label: 긍정 / 부정 / 중립 중 하나
                    - score: 0.0 ~ 1.0 사이의 신뢰도
                    - 가능한 경우 간단한 분석 근거 포함

                    ★ 출력 형식 — 아래 JSON형식으로만 출력하세요
                    {
                      "summary": "...",
                      "keywords": ["...", "..."],
                      "sentiment": {
                        "label": "긍정 | 부정 | 중립",
                        "score": 0.00
                      },
                      "domainKeywords": ["...", "..."]
                    }

                  
                    """;

            // ---------------------------------------------------------
            // 2) USER PROMPT
            // ---------------------------------------------------------
            String userPrompt = """
                    제목: %s

                    본문:
                    %s

                    위 요구사항을 100% 준수하여 JSON만 출력하세요.
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
            payload.put("max_tokens", 900);
            payload.put("temperature", 0.2);

            // ---------------------------------------------------------
            // 4) GPT API 호출
            // ---------------------------------------------------------
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(18));

            if (response == null) return Optional.empty();

            // ---------------------------------------------------------
            // 5) JSON 추출
            // ---------------------------------------------------------
            JsonNode root = mapper.readTree(response);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode()) return Optional.empty();

            String contentStr = content.asText().trim();

            // GPT가 ```json 블록으로 감쌀 경우 제거
            contentStr = contentStr
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();

            JsonNode parsed = mapper.readTree(contentStr);
            Map<String, Object> resultMap = mapper.convertValue(parsed, Map.class);

            return Optional.of(resultMap);

        } catch (Exception e) {
            System.err.println("🔥 GPT 분석 중 오류:");
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
