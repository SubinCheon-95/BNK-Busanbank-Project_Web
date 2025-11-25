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
import java.util.stream.Collectors;

@Service
public class NewsCrawlerService {

    private final ProductRepository productRepository;
    private final GPTAnalysisService gptService;
    private final OcrService ocrService;
    private final ObjectMapper mapper = new ObjectMapper();

    public NewsCrawlerService(ProductRepository productRepository,
                              GPTAnalysisService gptService,
                              OcrService ocrService) {
        this.productRepository = productRepository;
        this.gptService = gptService;
        this.ocrService = ocrService;
    }

    /**
     * URL 기반 분석 (크롤 → 규칙요약 → GPT 보완 → TF-IDF 코사인 추천)
     */
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
        // 초안 요약/키워드/감성 (규칙 기반)
        String summaryRule = summarise(body, 5);             // 기본 5문장
        List<String> keywordsRule = extractKeywords(body, 10);
        SentimentResult sentimentRule = analyzeSentiment(body);

        // GPT 보완 (있으면 사용)
        Optional<Map<String,Object>> gptOpt = gptService.analyzeWithGPT(title, body);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl(url);
        result.setTitle(title);
        result.setDescription(description);
        result.setImage(image);
        result.setSummary(summaryRule);
        result.setKeywords(keywordsRule);
        result.setSentiment(sentimentRule);

        // 추천상품: 코사인 유사도 기반 (뉴스 본문 + 도메인 키워드)
        List<ProductDTO> allProducts = productRepository.findAllForRecommendation();
        List<NewsAnalysisResult.ProductDto> recommended = recommendByCosineSimilarity(title, body, allProducts, 3);
        result.setRecommendations(recommended);

        // GPT 결과 병합 (우선순위: GPT 보완 > 규칙)
        gptOpt.ifPresent(map -> {
            if (map.get("summary") != null) result.setSummary(String.valueOf(map.get("summary")));
            if (map.get("keywords") != null) {
                try {
                    @SuppressWarnings("unchecked")
                    List<String> gkw = (List<String>) map.get("keywords");
                    if (gkw != null && !gkw.isEmpty()) result.setKeywords(gkw);
                } catch (Exception ignored){}
            }
            if (map.get("sentiment") != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String,Object> s = (Map<String,Object>) map.get("sentiment");
                    String label = String.valueOf(s.getOrDefault("label","중립"));
                    double score = 0.0;
                    try { score = Double.parseDouble(String.valueOf(s.getOrDefault("score","0"))); } catch(Exception ignored){}
                    result.setSentiment(new SentimentResult(label, score, "GPT 보완 분석"));
                } catch (Exception ignored){}
            }
            // GPT 추천상품이 제공되면 대체 (단, 여기선 우선 로컬 코사인 추천을 사용)
            if (map.get("recommendations") != null) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> gRec = (List<Map<String,Object>>) map.get("recommendations");
                    if (gRec != null && !gRec.isEmpty()) {
                        List<NewsAnalysisResult.ProductDto> gDtos = gRec.stream().map(m -> {
                            NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();
                            dto.setProductName(String.valueOf(m.getOrDefault("productName","추천상품")));
                            try { dto.setMaturityRate(Double.parseDouble(String.valueOf(m.getOrDefault("maturityRate","0")))); } catch(Exception e){ dto.setMaturityRate(0.0); }
                            dto.setDescription(String.valueOf(m.getOrDefault("description","")));
                            // productNo 없으면 0
                            try { dto.setProductNo(Long.parseLong(String.valueOf(m.getOrDefault("productNo","0")))); } catch(Exception e){ }
                            return dto;
                        }).collect(Collectors.toList());
                        result.setRecommendations(gDtos);
                    }
                } catch (Exception ignored) {}
            }
        });

        return result;
    }

    /**
     * 이미지 업로드 → OCR → 같은 로직으로 추천
     */
    public NewsAnalysisResult analyzeImage(MultipartFile file) throws Exception {
        String text = ocrService.extractText(file);
        if (text == null || text.isBlank()) throw new IllegalArgumentException("이미지에서 문자를 추출할 수 없습니다.");

        String summaryRule = summarise(text, 5);
        List<String> keywordsRule = extractKeywords(text, 10);
        SentimentResult sentimentRule = analyzeSentiment(text);

        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setUrl("IMAGE_UPLOAD");
        result.setTitle("업로드 이미지 분석 결과");
        result.setDescription("");
        result.setImage("");
        result.setSummary(summaryRule);
        result.setKeywords(keywordsRule);
        result.setSentiment(sentimentRule);

        // 추천상품: 코사인 유사도 기반
        List<ProductDTO> allProducts = productRepository.findAllForRecommendation();
        List<NewsAnalysisResult.ProductDto> recommended = recommendByCosineSimilarity(result.getTitle(), text, allProducts, 3);
        result.setRecommendations(recommended);

        // GPT 보완 (선택적)
        Optional<Map<String,Object>> gptOpt = gptService.analyzeWithGPT("기사 이미지", text);
        if (gptOpt.isPresent()) {
            Map<String,Object> map = gptOpt.get();
            if (map.get("summary") != null) result.setSummary(String.valueOf(map.get("summary")));
            if (map.get("keywords") != null) {
                try {
                    @SuppressWarnings("unchecked")
                    List<String> gkw = (List<String>) map.get("keywords");
                    if (gkw != null && !gkw.isEmpty()) result.setKeywords(gkw);
                } catch (Exception ignored){}
            }
        }

        return result;
    }

    // --------------------------------------------------------
    // 코사인 유사도 추천 핵심 로직
    // - TF-IDF 간단 구현을 사용하여 뉴스 <-> (상품명 + 설명) 유사도 계산
    // --------------------------------------------------------
    private List<NewsAnalysisResult.ProductDto> recommendByCosineSimilarity(String title, String body, List<ProductDTO> products, int topN) {
        // 1) 문서 리스트 구성: [뉴스 전체 텍스트] + products(each name+description)
        String newsText = (title == null ? "" : title) + " " + (body == null ? "" : body);
        List<String> docs = new ArrayList<>();
        docs.add(newsText);
        Map<Integer, ProductDTO> idxToProduct = new HashMap<>();
        int idx = 1;
        for (ProductDTO p : products) {
            String txt = (p.getProductName() == null ? "" : p.getProductName()) + " " + (p.getDescription() == null ? "" : p.getDescription());
            docs.add(txt);
            idxToProduct.put(idx, p);
            idx++;
        }

        // 2) TF-IDF 벡터화
        TfidfVectorizer vectorizer = new TfidfVectorizer();
        vectorizer.fit(docs);
        double[] newsVec = vectorizer.transformToArray(0);

        // 3) 각 상품과 코사인 유사도 계산
        List<ScoredProduct> scored = new ArrayList<>();
        for (int i = 1; i < docs.size(); i++) {
            double[] prodVec = vectorizer.transformToArray(i);
            double sim = VectorUtils.cosineSimilarity(newsVec, prodVec);
            ProductDTO prod = idxToProduct.get(i);
            scored.add(new ScoredProduct(prod, sim));
        }

        // 4) 상위 topN 선택
        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
                .limit(topN)
                .map(sp -> {
                    NewsAnalysisResult.ProductDto dto = new NewsAnalysisResult.ProductDto();
                    // productNo 타입 주의: ProductDTO 에서 타입(int/long)을 확인하고 변환 필요
                    try { dto.setProductNo(Long.valueOf(String.valueOf(sp.product.getProductNo()))); } catch(Exception e){}
                    dto.setProductName(sp.product.getProductName());
                    dto.setDescription(sp.product.getDescription());
                    dto.setMaturityRate(
                            sp.product.getMaturityRate() != null ? sp.product.getMaturityRate().doubleValue() : 0.0
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private static class ScoredProduct {
        ProductDTO product;
        double score;
        public ScoredProduct(ProductDTO product, double score) { this.product = product; this.score = score; }
        public double getScore(){ return score; }
    }

    // --------------------------------------------------------
    // HTML 크롤링 & 본문 추출 (기존)
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
    // 요약 / 키워드 / 감성 (기존 로직 유지, 필요시 개선 가능)
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

    private List<String> extractKeywords(String text, int topN) {
        if (text == null) return Collections.emptyList();
        String lowered = text.toLowerCase();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[가-힣]{2,}|[a-zA-Z]{2,}");
        java.util.regex.Matcher m = p.matcher(lowered);
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
}
