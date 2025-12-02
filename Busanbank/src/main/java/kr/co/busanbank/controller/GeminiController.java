/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : gemini 기능 처리 컨트롤러 작성
*/
package kr.co.busanbank.controller;


import kr.co.busanbank.dto.ChatbotDTO;
import kr.co.busanbank.service.ChatbotService;
import kr.co.busanbank.service.GeminiService;
import kr.co.busanbank.service.KomoranService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class GeminiController {
    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");
    private final KomoranService komoranService;
    private final ChatbotService chatbotService;
    private final GeminiService geminiService;

    @Value("${spring.gemini.api.key}")
    private String apiKey;

    public GeminiController(KomoranService komoranService, ChatbotService chatbotService, GeminiService geminiService) {
        this.komoranService = komoranService;
        this.chatbotService = chatbotService;
        this.geminiService = geminiService;
    }

//    @PostMapping("/member/chatbot")
//    public Mono<String> callGemini(@RequestBody Map<String, String> req) {
//        String input = req.get("message");
//        // 데이터 예시
//        String contextData = """
//        부산은행 홈페이지 주소 : http://localhost:8080/busanbank/member/chatbot
//        부산은행 이메일 : tjwjd010@naver.com
//    """;
//
//        // 모델에 보낼 프롬프트 구성
//        String prompt = """
//        아래 데이터 참고해서 답변해줘. 그리고 없는건 부산은행 사이트를 참고해줘
//        %s
//        질문: %s
//    """.formatted(contextData, input);
//
//        Map<String, Object> body = Map.of(
//                "contents", List.of(Map.of(
//                        "parts", List.of(Map.of("text", prompt))
//                ))
//        );
//
//        return webClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/v1beta/models/gemini-2.5-flash:generateContent")
//                        .queryParam("key", apiKey)
//                        .build())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(String.class);
//    }

    @PostMapping("/chatbot/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> req) throws Exception {

        String userMsg = req.get("message");
        log.info("userMsg: {}", userMsg);

        List<String> keywords = komoranService.extractKeywords(userMsg);
        keywords = chatbotService.refineKeywords(keywords);
        log.info("filtered keywords: {}", keywords);

        List<ChatbotDTO> relatedContents = chatbotService.findByKeywords(keywords);
        log.info("relatedContents: {}", relatedContents);

        String prompt = buildPrompt(userMsg, relatedContents);
        log.info("prompt: {}", prompt);

        String aiAnswer = geminiService.askGemini(prompt);
        //log.info("aiAnswer: {}", aiAnswer);

        return ResponseEntity.ok(Map.of("answer", aiAnswer));
    }

    private String buildPrompt(String userMsg, List<ChatbotDTO> contents) {

        StringBuilder sb = new StringBuilder();

        sb.append("사용자의 질문: ").append(userMsg).append("\n\n");

        sb.append("이 정보를 참고하여 질문과 직접 관련된 부분만 간단히 골라서 답변해줘. 우리 프로젝트명은 딸깍은행이야.\n\n");

        for (ChatbotDTO dto : contents) {
            sb.append("- [").append(dto.getCategory()).append("] ")
                    .append(dto.getContent())
                    .append("\n");
        }

        if (contents.isEmpty()) {
            sb.append("관련 지식 없음.\n");
        }

        return sb.toString();
    }


}