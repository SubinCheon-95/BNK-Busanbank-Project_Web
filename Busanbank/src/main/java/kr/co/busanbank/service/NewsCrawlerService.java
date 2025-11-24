package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.repository.ProductRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NewsCrawlerService {

    private final ProductRepository productRepository;
    private final GPTAnalysisService gptService;
    private final OcrService ocrService;

    public NewsCrawlerService(ProductRepository productRepository, GPTAnalysisService gptService, OcrService ocrService) {
        this.productRepository = productRepository;
        this.gptService = gptService;
        this.ocrService = ocrService;
    }

    public NewsAnalysisResult analyzeUrlWithAI(String url) throws IOException {

        if (url == null || url.isBlank()) throw new IllegalArgumentException("url is required");

        Document doc = fetchDocument(url);

        String title = Optional.ofNullable(doc.selectFirst("meta[property=og:title]"))
                .map(e -> e.attr("content")).orElse(doc.title());

        String description = Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                .map(e -> e.attr("content")).orElse("");

        String image = Optional.ofNullable(doc.selectFirst("meta[property=og:image]"))
                .map(e -> e.attr("content"))
                .orElseGet(() -> {
                    Element img = doc.selectFirst("img");
                    return img != null ? img.absUrl("src") : "";
                });

        String body = extractMainText(doc);
        String summaryRule = summarise(body, 3);
        List<String> keywordsRule = extractKeywords(body, 8);
        SentimentResult sentimentRule = analyzeSentiment(body);

        Optional<Map<String,Object>> gptOpt = gptService.analyzeWithGPT(title, body);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl(url);
        result.setTitle(title);
        result.setDescription(description);
        result.setImage(image);
        result.setSummary(summaryRule);
        result.setKeywords(keywordsRule);
        result.setSentiment(sentimentRule);

        // --- 추천상품 생성 ---
        List<ProductDTO> recs = recommendProducts(keywordsRule);
        List<NewsAnalysisResult.ProductDto> recDtos = recs.stream()
                .map(NewsCrawlerService::toDto)
                .collect(Collectors.toList());
        result.setRecommendations(recDtos);

        // GPT 결과 병합
        gptOpt.ifPresent(map -> {
            if (map.get("summary") != null) result.setSummary((String) map.get("summary"));
            if (map.get("keywords") != null) {
                result.setKeywords((List<String>) map.get("keywords"));
            }
            if (map.get("sentiment") != null) {
                Map<String,Object> s = (Map<String,Object>) map.get("sentiment");
                String label = s.getOrDefault("label","중립").toString();
                double score = 0.0;
                try { score = Double.parseDouble(s.getOrDefault("score","0").toString()); } catch(Exception ignored){}
                result.setSentiment(new SentimentResult(label, score, "GPT 보완 분석"));
            }
            if (map.get("recommendations") != null && ((List)map.get("recommendations")).size()>0) {

                List<Map<String,Object>> gRec = (List<Map<String,Object>>) map.get("recommendations");

                List<NewsAnalysisResult.ProductDto> gDtos = gRec.stream().map(m -> {
                    NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();
                    dto.setProductName(String.valueOf(m.getOrDefault("productName","추천상품")));
                    try { dto.setMaturityRate(Double.parseDouble(String.valueOf(m.getOrDefault("maturityRate","0")))); } catch(Exception e){ dto.setMaturityRate(0.0); }
                    dto.setDescription(String.valueOf(m.getOrDefault("description","")));
                    return dto;
                }).collect(Collectors.toList());

                result.setRecommendations(gDtos);
            }
        });

        return result;
    }


    // --------------------------------------------------------
    // HTML 크롤링
    // --------------------------------------------------------
    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; NewsCrawler/1.0)")
                .timeout(10_000)
                .get();
    }

    private String extractMainText(Document doc) {
        Element a = doc.selectFirst("article");
        if (a != null) return a.text();

        Element c = doc.selectFirst("[id*=content], [class*=content], [class*=article], [class*=article-body], [id*=article]");
        if (c != null) return c.text();

        return doc.body().text();
    }

    // --------------------------------------------------------
    // 요약
    // --------------------------------------------------------
    private String summarise(String text, int nSentences) {
        if (text == null || text.isEmpty()) return "";
        List<String> sentences = splitSentences(text);
        return sentences.stream().limit(nSentences).collect(Collectors.joining(" "));
    }

    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.KOREAN);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String s = text.substring(start, end).trim();
            if (!s.isEmpty()) sentences.add(s);
        }
        if (sentences.isEmpty()) {
            for (String s : text.split("\\. ")) if (!s.isEmpty()) sentences.add(s);
        }
        return sentences;
    }

    // --------------------------------------------------------
    // 키워드 추출
    // --------------------------------------------------------
    private List<String> extractKeywords(String text, int topN) {
        if (text == null) return Collections.emptyList();

        String lowered = text.toLowerCase();
        Pattern p = Pattern.compile("[가-힣]{2,}|[a-zA-Z]{2,}");
        Matcher m = p.matcher(lowered);

        Map<String,Integer> freq = new HashMap<>();
        Set<String> stop = koreanStopwords();

        while (m.find()) {
            String w = m.group();
            if (stop.contains(w)) continue;
            freq.put(w, freq.getOrDefault(w,0)+1);
        }

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> koreanStopwords() {
        return new HashSet<>(Arrays.asList(
                "그리고","하지만","때문에","그럼","그","이","저","는","의","에","을","를",
                "있다","했다","합니다","입니다","있습니다","것","수"
        ));
    }

    // --------------------------------------------------------
    // 감성 분석
    // --------------------------------------------------------
    private SentimentResult analyzeSentiment(String text) {
        if (text == null || text.isEmpty())
            return new SentimentResult("중립", 0.0, "본문이 없어 분석 불가");

        int score = 0;
        String lower = text.toLowerCase();

        String[] pos = {"상승","호전","증가","안정","우대","혜택","이익","상향","호조","증대"};
        String[] neg = {"하락","우려","불안","문제","부담","감소","악화","손실","불리","약세","위기"};

        for (String s: pos) if (lower.contains(s)) score += 2;
        for (String s: neg) if (lower.contains(s)) score -= 2;

        String label = (score > 1) ? "긍정" : (score < -1) ? "부정" : "중립";
        return new SentimentResult(label, score, "규칙 기반 분석");
    }

    // --------------------------------------------------------
    // 추천 상품 계산
    // --------------------------------------------------------
    private List<ProductDTO> recommendProducts(List<String> keywords) {

        boolean wantsSaving = keywords.stream()
                .anyMatch(k -> k.contains("적금") || k.contains("저축") || k.contains("예금"));

        if (wantsSaving) {
            List<ProductDTO> sav = productRepository.findTopSavingsByRate(5);
            if (!sav.isEmpty()) return sav;
        }

        return productRepository.findTopByOrderByMaturityRateDesc(3);
    }


    // --------------------------------------------------------
    // 🔥 여기 수정된 toDto() 메서드 (문제 해결됨)
    // --------------------------------------------------------
    private static NewsAnalysisResult.ProductDto toDto(ProductDTO p) {
        if (p == null) return null;

        NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();

        // int → Long 변환
        dto.setProductNo(Long.valueOf(p.getProductNo()));

        // BigDecimal → double 변환
        dto.setMaturityRate(
                p.getMaturityRate() != null
                        ? p.getMaturityRate().doubleValue()
                        : 0.0
        );

        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());

        return dto;
    }

    public NewsAnalysisResult analyzeImage(MultipartFile file) throws Exception {

        // 1) 이미지 → 텍스트(OCR)
        String text = ocrService.extractText(file);  // 직접 구현한 OCR 서비스 주입

        if (text == null || text.isBlank())
            throw new IllegalArgumentException("이미지에서 문자를 추출할 수 없습니다.");

        // 2) 요약 / 키워드 / 감정 분석 등 기존 로직 재사용
        String summary = summarise(text, 3);
        List<String> keywords = extractKeywords(text, 8);
        SentimentResult sentiment = analyzeSentiment(text);

        // GPT 보완 분석
        Optional<Map<String,Object>> gptOpt = gptService.analyzeWithGPT("기사 이미지", text);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl("IMAGE_UPLOAD");
        result.setTitle("업로드 이미지 분석 결과");
        result.setDescription("");
        result.setImage("");
        result.setSummary(summary);
        result.setKeywords(keywords);
        result.setSentiment(sentiment);

        // 추천상품 로직 동일
        List<ProductDTO> recs = recommendProducts(keywords);
        List<NewsAnalysisResult.ProductDto> recDtos = recs.stream()
                .map(NewsCrawlerService::toDto)
                .collect(Collectors.toList());
        result.setRecommendations(recDtos);

        return result;
    }


}
