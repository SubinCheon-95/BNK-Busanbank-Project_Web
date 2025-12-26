package kr.co.busanbank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import kr.co.busanbank.service.RuleBasedSentimentAnalyzer;  // ✅ 추가!
import java.util.stream.Collectors;

@Service
public class NewsCrawlerService {

    private final ProductRepository productRepository;
    private final GPTAnalysisService gptService;
    private final OcrService ocrService;
    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ RuleBasedSentimentAnalyzer 추가!
    private final RuleBasedSentimentAnalyzer sentimentAnalyzer = new RuleBasedSentimentAnalyzer();

    public NewsCrawlerService(ProductRepository productRepository,
                              GPTAnalysisService gptService,
                              OcrService ocrService) {
        this.productRepository = productRepository;
        this.gptService = gptService;
        this.ocrService = ocrService;
    }

    // ============================================================
    // 🔥 URL 기반 기사 분석
    // ============================================================
    public NewsAnalysisResult analyzeUrlWithAI(String url) throws IOException {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("url is required");

        Document doc = fetchDocument(url);

        String title = extractTitle(doc);
        String description = extractDescription(doc);
        String image = extractImage(doc);
        String body = extractMainText(doc);

        // 규칙 기반 분석
        String summaryRule = summarise(body, 6);
        List<String> keywordsRule = extractKeywords(body, 12);
        SentimentResult sentimentRule = analyzeSentiment(body);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl(url);
        result.setTitle(title);
        result.setDescription(description);
        result.setImage(image);
        result.setSummary(summaryRule);
        result.setKeywords(keywordsRule);
        result.setSentiment(sentimentRule);

        // 🔥 추천상품: 코사인 유사도
        List<ProductDTO> allProducts = productRepository.findAllForRecommendation();
        List<NewsAnalysisResult.ProductDto> recommended = recommendByCosineSimilarity(title, body, allProducts, 3);
        result.setRecommendations(recommended);

        // GPT 분석 → 보완
        mergeGPTAnalysis(result, title, body);

        return result;
    }

    // ============================================================
    // 🔥 이미지 기반 기사 분석 (OCR)
    // ============================================================
    public NewsAnalysisResult analyzeImage(MultipartFile file) throws Exception {
        String text = ocrService.extractText(file);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("이미지에서 텍스트 추출 불가");
        }

        String summaryRule = summarise(text, 5);
        List<String> keywordsRule = extractKeywords(text, 10);
        SentimentResult sentimentRule = analyzeSentiment(text);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl("IMAGE_UPLOAD");
        result.setTitle("이미지 분석 결과");
        result.setSummary(summaryRule);
        result.setKeywords(keywordsRule);
        result.setSentiment(sentimentRule);

        // 코사인 추천
        List<ProductDTO> allProducts = productRepository.findAllForRecommendation();
        List<NewsAnalysisResult.ProductDto> recommended =
                recommendByCosineSimilarity("이미지 기사", text, allProducts, 3);
        result.setRecommendations(recommended);

        // GPT 보완
        mergeGPTAnalysis(result, "이미지 기사", text);

        return result;
    }

    // ============================================================
    // 🔥 GPT 결과 결합 로직 (강화됨)
    // ============================================================
    private void mergeGPTAnalysis(NewsAnalysisResult result, String title, String body) {
        Optional<Map<String, Object>> gptOpt = gptService.analyzeWithGPT(title, body);

        if (!gptOpt.isPresent()) return;

        Map<String, Object> g = gptOpt.get();

        // 요약 보완
        if (g.get("summary") != null) {
            String gsum = String.valueOf(g.get("summary"));
            if (gsum.length() > result.getSummary().length()) {
                result.setSummary(gsum);
            }
        }

        // 키워드 보완
        if (g.get("keywords") != null) {
            try {
                @SuppressWarnings("unchecked")
                List<String> gkw = (List<String>) g.get("keywords");
                if (gkw.size() > result.getKeywords().size())
                    result.setKeywords(gkw);
            } catch (Exception ignored) {}
        }

        // 감성 분석
        if (g.get("sentiment") != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> s = (Map<String, Object>) g.get("sentiment");

                String label = String.valueOf(s.getOrDefault("label", "중립"));
                double score = Double.parseDouble(String.valueOf(s.getOrDefault("score", "0")));

                // ✅ GPT에서 matchedWords 받기
                List<String> gptPositiveWords = new ArrayList<>();
                List<String> gptNegativeWords = new ArrayList<>();

                if (s.get("matchedPositiveWords") != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<String> words = (List<String>) s.get("matchedPositiveWords");
                        gptPositiveWords.addAll(words);
                    } catch (Exception ignored) {}
                }

                if (s.get("matchedNegativeWords") != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<String> words = (List<String>) s.get("matchedNegativeWords");
                        gptNegativeWords.addAll(words);
                    } catch (Exception ignored) {}
                }

                // ✅ 전략: GPT 단어가 있으면 GPT 우선, 없으면 RuleBased 유지
                SentimentResult current = result.getSentiment();

                List<String> finalPositive = gptPositiveWords.isEmpty()
                        ? (current != null ? current.getMatchedPositiveWords() : new ArrayList<>())
                        : gptPositiveWords;

                List<String> finalNegative = gptNegativeWords.isEmpty()
                        ? (current != null ? current.getMatchedNegativeWords() : new ArrayList<>())
                        : gptNegativeWords;

                result.setSentiment(new SentimentResult(
                        label,
                        score,
                        "GPT 보완 분석",
                        finalPositive,
                        finalNegative
                ));

                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("✅ GPT 감성 분석 병합:");
                System.out.println("   Label: " + label);
                System.out.println("   긍정 단어: " + finalPositive);
                System.out.println("   부정 단어: " + finalNegative);
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━");

            } catch (Exception e) {
                System.err.println("❌ GPT 감성 분석 병합 실패: " + e.getMessage());
            }
        }

        // GPT 추천상품(선택) — 기본은 코사인 유사도 유지
        if (g.get("recommendations") != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> gRec = (List<Map<String, Object>>) g.get("recommendations");
                if (!gRec.isEmpty()) {
                    List<NewsAnalysisResult.ProductDto> list =
                            gRec.stream().map(m -> {
                                NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();
                                dto.setProductName(String.valueOf(m.get("productName")));
                                dto.setDescription(String.valueOf(m.get("description")));
                                try {
                                    dto.setMaturityRate(
                                            Double.parseDouble(String.valueOf(m.get("maturityRate")))
                                    );
                                } catch (Exception ignore) {
                                    dto.setMaturityRate(0.0);
                                }
                                return dto;
                            }).collect(Collectors.toList());
                    result.setRecommendations(list);
                }
            } catch (Exception ignored) {}
        }
    }

    // ============================================================
    // 🔥 코사인 유사도 + TF-IDF 추천 (강화 버전)
    // ============================================================
    private List<NewsAnalysisResult.ProductDto> recommendByCosineSimilarity(
            String title,
            String body,
            List<ProductDTO> products,
            int topN
    ) {
        // 뉴스 텍스트: 제목 가중치 강화
        String newsText = (title + " " + title + " " + body).trim();

        List<String> docs = new ArrayList<>();
        docs.add(newsText);

        Map<Integer, ProductDTO> indexMap = new HashMap<>();
        int idx = 1;

        for (ProductDTO p : products) {

            String text =
                    (p.getProductName() + " " + p.getProductName() + " " +
                            (p.getDescription() == null ? "" : p.getDescription()) + " " +
                            (p.getProductFeatures() == null ? "" : p.getProductFeatures()))
                            .trim();

            docs.add(text);
            indexMap.put(idx, p);
            idx++;
        }

        // TF-IDF 벡터화
        TfidfVectorizer vectorizer = new TfidfVectorizer();
        vectorizer.fit(docs);
        double[] newsVec = vectorizer.transformToArray(0);

        // 유사도 계산
        List<ScoredProduct> scored = new ArrayList<>();
        for (int i = 1; i < docs.size(); i++) {
            double[] vec = vectorizer.transformToArray(i);
            double sim = VectorUtils.cosineSimilarity(newsVec, vec);
            scored.add(new ScoredProduct(indexMap.get(i), sim));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
                .limit(topN)
                .map(sp -> {
                    ProductDTO p = sp.product;
                    NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();
                    dto.setProductNo((long) p.getProductNo());
                    dto.setProductName(p.getProductName());
                    dto.setDescription(p.getDescription());
                    dto.setMaturityRate(
                            p.getMaturityRate() != null
                                    ? p.getMaturityRate().doubleValue()
                                    : 0
                    );
                    return dto;
                }).collect(Collectors.toList());
    }

    private static class ScoredProduct {
        public ProductDTO product;
        public double score;
        public ScoredProduct(ProductDTO p, double s) { product = p; score = s; }
        public double getScore() { return score; }
    }

    // ============================================================
    // 🔥 크롤링 강화 (본문 정확도 향상)
    // ============================================================
    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; NewsCrawler/2.0)")
                .timeout(12000)
                .get();
    }

    private String extractTitle(Document doc) {
        Element e = doc.selectFirst("meta[property=og:title]");
        if (e != null && !e.attr("content").isBlank()) return e.attr("content");
        return doc.title();
    }

    private String extractDescription(Document doc) {
        Element e = doc.selectFirst("meta[name=description]");
        return e != null ? e.attr("content") : "";
    }

    private String extractImage(Document doc) {
        Element e = doc.selectFirst("meta[property=og:image]");
        if (e != null) return e.attr("content");
        Element img = doc.selectFirst("img");
        return img != null ? img.absUrl("src") : "";
    }

    // 🔥 본문 추출 알고리즘 개선됨
    private String extractMainText(Document doc) {

        List<String> selectors = Arrays.asList(
                "article",
                ".article", "#article",
                ".article-body", "#article-body",
                "[id*=content]", "[class*=content]",
                ".news_cnt_detail_wrap",
                ".news_contents",
                ".text", ".view"
        );

        for (String sel : selectors) {
            Element block = doc.selectFirst(sel);
            if (block != null) {
                String text = block.text();
                if (text.length() > 100) return text;
            }
        }

        return doc.body().text();
    }

    // ============================================================
    // 요약 / 키워드 / 감성 (내장 규칙)
    // ============================================================
    private String summarise(String text, int sentences) {
        if (text == null || text.isBlank()) return "";
        List<String> list = splitSentences(text);
        return list.stream().limit(sentences).collect(Collectors.joining(" "));
    }

    private List<String> splitSentences(String text) {
        List<String> out = new ArrayList<>();
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.KOREAN);
        it.setText(text);
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String s = text.substring(start, end).trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private List<String> extractKeywords(String text, int limit) {
        if (text == null) return Collections.emptyList();
        String lower = text.toLowerCase();

        java.util.regex.Pattern p =
                java.util.regex.Pattern.compile("[가-힣]{2,}|[a-zA-Z]{2,}");
        java.util.regex.Matcher m = p.matcher(lower);

        Map<String, Integer> freq = new HashMap<>();
        Set<String> stop = koreanStopwords();

        while (m.find()) {
            String w = m.group();
            if (!stop.contains(w)) {
                freq.put(w, freq.getOrDefault(w, 0) + 1);
            }
        }

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> koreanStopwords() {
        return new HashSet<>(Arrays.asList(
                "그리고", "하지만", "때문에", "그", "이", "저", "는", "의",
                "에", "을", "를", "있다", "했다", "합니다", "입니다",
                "있습니다", "것", "수", "등", "로", "또한", "또"
        ));
    }

    private SentimentResult analyzeSentiment(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentResult("중립", 0.0, "본문 없음",
                    new ArrayList<>(), new ArrayList<>());
        }

        // ✅ RuleBasedSentimentAnalyzer 사용!
        return sentimentAnalyzer.analyze(text);
    }
}