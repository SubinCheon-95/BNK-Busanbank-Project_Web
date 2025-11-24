package kr.co.busanbank.controller;


import kr.co.busanbank.service.NewsAnalysisResult;
import kr.co.busanbank.service.NewsCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/crawl")
public class CrawlController {

    private final NewsCrawlerService crawlerService;

    @Autowired
    public CrawlController(NewsCrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/news")
    public ResponseEntity<?> analyze(@RequestBody CrawlRequest req) {
        try {
            NewsAnalysisResult result = crawlerService.analyzeUrlWithAI(req.getUrl());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("크롤링/분석 오류: " + e.getMessage());
        }
    }

    public static class CrawlRequest {
        private String url;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    @PostMapping("/news/image")
    public ResponseEntity<?> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            NewsAnalysisResult result = crawlerService.analyzeImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("이미지 분석 오류: " + e.getMessage());
        }
    }
}
