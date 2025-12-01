package kr.co.busanbank.service;

import java.util.*;

/**
 * 2025.12.01 - 수진 (확장판)
 * 금융/경제 기사에 최적화된 룰 기반 감정 분석기
 * - 대폭 확장된 긍정/부정 사전 포함 (약 150개 이상)
 * - 부정어 처리 (“않”, “못”, “없”, “아니” 등)
 * - 단어 기반 스코어링
 */
public class RuleBasedSentimentAnalyzer {

    // -----------------------------
    // 1) 감성 사전 (금융/경제 특화)
    // -----------------------------
    private static final Set<String> positiveWords = Set.of(
            "호조","상승","개선","확대","증가","강세","회복","기대","성장","최고","안정","강화","발표","호황",
            "급등","신기록","돌파","호재","견조","양호","활성화","확장","강력","탄탄","순증","부흥","개발",
            "도약","상향","추진","혁신","투자확대","인상","흑자","실적개선","이익증가","유입","수요증가",
            "활황","사상최대","역대최대","호평","긍정적","회복세","강력한","상승세","호전","성공","모멘텀",
            "안정세","완화","순항","호전세","개량","특수","급증","신장","유리","강력","효과","양성"
    );

    private static final Set<String> negativeWords = Set.of(
            "하락","악화","감소","부진","위기","약세","불안","급락","손실","경고","우려","실망","파산","부담",
            "침체","위축","둔화","역성장","약화","급감","적자","손해","악재","부정적","불확실성","경기둔화",
            "밀림","실패","추락","불황","경영악화","충격","과열","리스크","절벽","고점경고","공포","패닉",
            "급감","폐업","축소","퇴보","위험","불만","비난","논란","분쟁","경고등","마이너스","급감","고통",
            "금리인상 부담","채무불이행","체불","북새통","물가상승","인플레이션","디플레이션"
    );

    // -----------------------------
    // 2) 부정어 (NOT 처리)
    // -----------------------------
    private static final List<String> negationWords = List.of(
            "않", "못", "없", "아니", "부정", "미흡", "불"
    );

    /**
     * 메인 감정 분석 함수
     */
    public SentimentResult analyze(String text) {

        if (text == null || text.isBlank()) {
            return new SentimentResult("중립", 0.5, "내용 없음");
        }

        // 전처리
        String cleaned = text.replaceAll("[^\\p{L}\\p{Nd}]+", " ").toLowerCase();
        String[] tokens = cleaned.split("\\s+");

        int score = 0;
        boolean lastWasNegation = false;

        for (String token : tokens) {

            // 1) 부정어 체크
            boolean isNegation = negationWords.stream().anyMatch(token::contains);
            if (isNegation) {
                lastWasNegation = true;
                continue;
            }

            // 2) 긍정 단어
            if (positiveWords.contains(token)) {
                if (lastWasNegation) score -= 2;   // NOT + positive
                else score += 2;
            }

            // 3) 부정 단어
            if (negativeWords.contains(token)) {
                if (lastWasNegation) score += 2;   // NOT + negative → 긍정
                else score -= 2;
            }

            lastWasNegation = false;
        }

        // -----------------------------
        // 점수 → 감정 레이블 변환
        // -----------------------------
        String label;
        double confidence = Math.min(1.0, Math.abs(score) / 10.0);

        if (score > 1) label = "긍정";
        else if (score < -1) label = "부정";
        else label = "중립";

        String explain = String.format("룰 기반 감정분석: score=%d, confidence=%.2f", score, confidence);

        return new SentimentResult(label, confidence, explain);
    }
}
