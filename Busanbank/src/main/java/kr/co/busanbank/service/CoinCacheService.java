package kr.co.busanbank.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CoinCacheService {

    private final CoinApiService coinApiService;
    private List<Map<String,Object>> cachedData = new ArrayList<>();
    private long lastUpdated = 0;

    public CoinCacheService(CoinApiService coinApiService) {
        this.coinApiService = coinApiService;
    }

    public List<Map<String,Object>> getLatest() {
        long now = System.currentTimeMillis();
        if (now - lastUpdated > 3_600_000) { // 1시간
            cachedData = coinApiService.fetchFromCMC();
            lastUpdated = now;
        }
        return cachedData;
    }
}
