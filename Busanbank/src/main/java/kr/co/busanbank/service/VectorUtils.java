package kr.co.busanbank.service;

public class VectorUtils {

    public static double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null) return 0.0;
        int n = Math.min(a.length, b.length);
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
