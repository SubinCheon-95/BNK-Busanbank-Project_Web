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

    // "${spring.openai.api-key:}" 여기서 spring 부분 app으로 바꾸면
    // GPT가 작동되지않고 룰베이스드로 전환.
    public GPTAnalysisService(@Value("${spring.openai.api-key:}") String openaiApiKey) {

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
                      4-1) 감정 점수는 0 ~ 10 사이의 값으로 출력
                      5) 감성 분석 시 매칭된 긍정/부정 단어 리스트도 추출/중복금지/
                      5-1) 긍정일때 단어10개 중복금지 추출/부정일때 단어 10개 중복금지 추출/
                      중립일때 긍정단어 5개,부정단어 5개 중복금지 추출
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
                    
                    ** 감성 분석 단어 추출 규칙 (Loughran-McDonald 금융 감성 사전 기반):
                    
                    ✅ 긍정 단어 (187개):
                    호조,상승,개선,확대,증가,강세,회복,기대,성장,최고,안정,강화,발표,급등,신기록,돌파,호재,견조,양호,활성화,확장,강력,탄탄,순증,부흥,개발,도약,상향,추진,혁신,투자확대,인상,흑자,이익증가,유입,수요증가,활황,사상최대,역대최대,호평,긍정적,회복세,강력한,상승세,호전,성공,모멘텀,안정세,완화,순항,호전세,개량,특수,급증,신장,유리,효과,양성,번영,활기,부양,선순환,활황세,탄력,반등,개선세,상승전환,턴어라운드,돌파구,개선효과,회복탄력,성장동력,성장잠재력,수익성,수익확대,이익률,순이익,매출증가,매출신장,매출호조,실적호조,실적개선,실적호전,업황개선,영업이익,매수,상한가,강세장,상승장,랠리,급등세,강세전환,상승탄력,상승모멘텀,투자매력,투자가치,고평가,프리미엄,수익률,배당,배당금,자사주매입,유상증자,신고가,최고가,신기록경신,역대최고,사상최고,기록경신,연속상승,연속급등,규제완화,지원책,부양책,인센티브,보조금,세제혜택,세액공제,감세,세율인하,금리인하,기준금리인하,통화완화,양적완화,유동성공급,경기부양,경기활성화,신기술,특허,기술개발,연구개발,신제품,출시,론칭,시장진출,시장확대,점유율확대,경쟁력,브랜드가치,고객만족,생산성,효율성,구조조정성공,비용절감,낙관,낙관적,긍정전망,상승전망,호전전망,밝음,밝은전망,기대감,신뢰,확신,희망,희망적,가능성,잠재력,기회,찬스,청신호,호기,적기,호조세,강세전망,기회확대,달성,등급상향,매출,상승중,상향조정,상회,성공적,성취,수익,시장대비상승,실적,우수한실적,우호적,유망,유망한,이득,이익,증가세,초과,초과달성
                    
                    ❌ 부정 단어 (209개):
                    하락,악화,감소,부진,위기,약세,불안,급락,손실,경고,우려,실망,파산,부담,침체,위축,둔화,역성장,약화,급감,적자,손해,악재,부정적,불확실성,경기둔화,밀림,실패,추락,불황,경영악화,충격,과열,리스크,절벽,고점경고,공포,폐업,축소,퇴보,위험,불만,비난,논란,분쟁,경고등,마이너스,고통,금리인상부담,채무불이행,체불,북새통,물가상승,인플레이션,디플레이션,경기침체,경기위축,경기후퇴,불경기,장기침체,경기하강,경기악화,마이너스성장,디플레,스태그플레이션,버블,거품,붕괴,붕괴위기,시장혼란,시장불안,시장공포,공황,패닉,폭락,폭락장,약세장,하락장,베어마켓,조정,급락세,하락전환,매도,하한가,약세전환,하락탄력,하락모멘텀,투자손실,손실확대,평가손,손절,저평가,디스카운트,수익률하락,배당축소,배당감소,감자,유상감자,상장폐지,신저가,최저가,연속하락,연속급락,폭락세,추락세,곤두박질,폭락장세,규제강화,규제,단속,제재,과징금,벌금,조사,조치,제한,금지,중단,중지,금리인상,기준금리인상,긴축,통화긴축,유동성회수,경기긴축,긴축정책,긴축기조,도산,부도,회생,워크아웃,구조조정,정리해고,감원,인력감축,공장폐쇄,생산중단,생산차질,재고증가,재고누적,매출감소,매출급감,매출부진,실적부진,실적악화,실적쇼크,영업손실,순손실,적자전환,적자누적,적자폭확대,사고,결함,리콜,소송,갈등,마찰,대립,반발,항의,비판,부패,비리,횡령,배임,사기,조작,부정,위반,탈세,회계부정,분식회계,비관,비관적,부정전망,하락전망,악화전망,어둡다,어두운전망,두려움,걱정,염려,회의,회의적,의문,의혹,불신,위기감,경계,경계심,적신호,곤경,공포심,등급하향,매도세,미달,불안감,불확실,시장대비하락,실망스러운,약세전망,어려움,역풍,우려확산,위험요인,위협,저조한실적,취약,하향조정
                    
                    ⚠️ 중요 (반드시 준수!):
                    1. 위 단어 사전 선택우선 + 기사에 실재 나오는 감정 단어
                    2. 기사에 실제로 등장한 단어만
                    3. 중복 절대 금지! (같은 단어 1번만!)
                       예: ["상승", "상승", "상승"] ❌
                           ["상승", "급등", "호재"] ✅
                    4. 정확히 10개! (긍정 10개, 부정 10개), 중복금지!
                       11개 ❌, 9개  ❌
                       4-1. 중립감정일 경우: 긍정 5개, 부정 5개, 중복금지!
                    5. 핵심 단어 우선 (중요도 순)
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