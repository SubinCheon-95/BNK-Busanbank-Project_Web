package kr.co.busanbank.service;

import java.util.List;

public class SentimentResult {
    private String label;
    private double score;
    private String explain;

    // ✅ 매칭된 단어 리스트 추가!
    private List<String> matchedPositiveWords;
    private List<String> matchedNegativeWords;

    public SentimentResult() {}

    public SentimentResult(String label, double score, String explain) {
        this.label = label;
        this.score = score;
        this.explain = explain;
    }

    // ✅ 전체 생성자
    public SentimentResult(String label, double score, String explain,
                           List<String> matchedPositiveWords,
                           List<String> matchedNegativeWords) {
        this.label = label;
        this.score = score;
        this.explain = explain;
        this.matchedPositiveWords = matchedPositiveWords;
        this.matchedNegativeWords = matchedNegativeWords;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getExplain() { return explain; }
    public void setExplain(String explain) { this.explain = explain; }

    // ✅ 새 필드 getter/setter
    public List<String> getMatchedPositiveWords() { return matchedPositiveWords; }
    public void setMatchedPositiveWords(List<String> matchedPositiveWords) {
        this.matchedPositiveWords = matchedPositiveWords;
    }

    public List<String> getMatchedNegativeWords() { return matchedNegativeWords; }
    public void setMatchedNegativeWords(List<String> matchedNegativeWords) {
        this.matchedNegativeWords = matchedNegativeWords;
    }
}