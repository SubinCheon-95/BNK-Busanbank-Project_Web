package kr.co.busanbank.service;

import java.util.*;

/**
 *  2025.11.25 김수진
 * 간단한 TF-IDF 벡터라이저
 * - fit(docs): 단어 사전 구성 및 IDF 계산
 * - transformToArray(i): 문서 인덱스 i의 TF-IDF 배열 반환
 *
 * *주의*: 대형 코퍼스에서는 메모리/성능 이슈 발생 가능.
 * 더 정확한 결과는 Apache Lucene, Elasticsearch, 또는 외부 임베딩(문장 임베딩) 사용해야한다고 함.
 */
public class TfidfVectorizer {

    private List<String> vocabList = new ArrayList<>();
    private Map<String,Integer> vocabIndex = new HashMap<>();
    private double[] idf;  // vocab 길이

    private List<Map<String,Integer>> termCounts = new ArrayList<>();
    private int docCount = 0;

    public void fit(List<String> docs) {
        docCount = docs.size();
        vocabList.clear();
        vocabIndex.clear();
        termCounts.clear();

        Map<String,Integer> dfCounter = new HashMap<>();

        for (String doc : docs) {
            Map<String,Integer> counts = new HashMap<>();
            String[] tokens = tokenize(doc);
            Set<String> seen = new HashSet<>();
            for (String t : tokens) {
                if (t.isBlank()) continue;
                counts.put(t, counts.getOrDefault(t,0)+1);
                if (!seen.contains(t)) { dfCounter.put(t, dfCounter.getOrDefault(t,0)+1); seen.add(t); }
            }
            termCounts.add(counts);
        }

        // build vocab sorted by df desc (optional)
        List<String> vocab = new ArrayList<>(dfCounter.keySet());
        Collections.sort(vocab);
        int idx = 0;
        for (String token : vocab) {
            vocabIndex.put(token, idx++);
            vocabList.add(token);
        }

        // compute IDF
        idf = new double[vocabList.size()];
        for (int i = 0; i < vocabList.size(); i++) {
            String term = vocabList.get(i);
            int df = dfCounter.getOrDefault(term, 1);
            idf[i] = Math.log((double)(docCount + 1) / (df + 1)) + 1.0;
        }
    }

    public double[] transformToArray(int docIndex) {
        if (docIndex < 0 || docIndex >= termCounts.size()) return new double[vocabList.size()];
        double[] vec = new double[vocabList.size()];
        Map<String,Integer> counts = termCounts.get(docIndex);
        int totalTerms = counts.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String,Integer> e : counts.entrySet()) {
            String term = e.getKey();
            Integer idx = vocabIndex.get(term);
            if (idx == null) continue;
            double tf = (double)e.getValue() / Math.max(1, totalTerms);
            vec[idx] = tf * idf[idx];
        }
        return vec;
    }

    private String[] tokenize(String text) {
        if (text == null) return new String[0];
        // 간단 토크나이저: 비문자 제거 후 split
        String cleaned = text.replaceAll("[^\\p{L}\\p{Nd}]+", " ").toLowerCase();
        return cleaned.split("\\s+");
    }
}
