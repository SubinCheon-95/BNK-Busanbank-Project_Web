package kr.co.busanbank.service;


import java.util.List;

public class NewsAnalysisResult {
    private String url;
    private String title;
    private String description;
    private String image;
    private String summary;
    private List<String> keywords;
    private SentimentResult sentiment;
    private List<ProductDto> recommendations;

    // getters / setters

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public SentimentResult getSentiment() { return sentiment; }
    public void setSentiment(SentimentResult sentiment) { this.sentiment = sentiment; }
    public List<ProductDto> getRecommendations() { return recommendations; }
    public void setRecommendations(List<ProductDto> recommendations) { this.recommendations = recommendations; }

    public static class ProductDto {
        private Long productNo;
        private String productName;
        private Double maturityRate;
        private String description;
        // getters / setters
        public Long getProductNo() { return productNo; }
        public void setProductNo(Long productNo) { this.productNo = productNo; }
        public String getProductName() { return productName;}
        public void setProductName(String productName) { this.productName = productName;}
        public Double getMaturityRate() { return maturityRate; }
        public void setMaturityRate(Double maturityRate) { this.maturityRate = maturityRate; }
        public String getDescription(){return description;}
        public void setDescription(String description){this.description = description;}
    }
}