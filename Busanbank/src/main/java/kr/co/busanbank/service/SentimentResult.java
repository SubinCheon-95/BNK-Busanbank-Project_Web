package kr.co.busanbank.service;

public class SentimentResult {
    private String label;
    private double score;
    private String explain;

    public SentimentResult() {}
    public SentimentResult(String label, double score, String explain) {
        this.label = label; this.score = score; this.explain = explain;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getExplain() { return explain; }
    public void setExplain(String explain) { this.explain = explain; }
}
