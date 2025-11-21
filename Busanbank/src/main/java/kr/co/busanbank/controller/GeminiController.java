/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : gemini 기능 처리 컨트롤러 작성
*/
package kr.co.busanbank.controller;


import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
public class GeminiController {
    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");

    @Value("${spring.gemini.api.key}")
    private String apiKey;

    @PostMapping("/member/chatbot")
    public Mono<String> callGemini(@RequestBody String input) {

        // 데이터 예시
        String contextData = """
        부산은행 홈페이지 주소 : http://localhost:8080/busanbank/member/chatbot
        부산은행 이메일 : tjwjd010@naver.com
    """;

        // 모델에 보낼 프롬프트 구성
        String prompt = """
        아래 데이터 참고해서 답변해줘. 그리고 없는건 부산은행 사이트를 참고해줘
        %s
        질문: %s
    """.formatted(contextData, input);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-2.5-flash:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

}