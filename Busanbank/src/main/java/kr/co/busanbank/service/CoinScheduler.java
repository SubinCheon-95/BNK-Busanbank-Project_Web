package kr.co.busanbank.service;

import kr.co.busanbank.mapper.PriceHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class CoinScheduler {
    private final CoinApiService coinApiService;
    private final PriceHistoryMapper priceHistoryMapper;
    private boolean running = false;

    // 비트코인: 매시 정각 실행
    @Scheduled(cron = "0 0 * * * *")
    public void saveCoinHourly() {
        List<Map<String, Object>> data = coinApiService.fetchFromCMC();
        saveOne(data, "BTC");
    }

    // 금: 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void saveGoldDaily() {
        Map<String, Double> metalData = coinApiService.fetchMetalPrice();
        priceHistoryMapper.insertPrice("XAU", metalData.get("USDXAU"));
    }

    //기름:
    @Scheduled(cron = "0 0 * * * *")
    public void saveOilPrice() {
        Map<String,Object> oil = coinApiService.fetchOilPrice();
        double price = Double.parseDouble(oil.get("price").toString());

        priceHistoryMapper.insertPrice("OIL", price);
    }

    private void saveOne(List<Map<String,Object>> data, String symbol) {
        Map<String,Object> item = data.stream()
                .filter(c -> c.get("symbol").equals(symbol))
                .findFirst()
                .orElse(null);

        if (item == null) return;

        Map quote = (Map) item.get("quote");
        Map usd = (Map) quote.get("USD");

        Double price = ((Number)usd.get("price")).doubleValue();

        priceHistoryMapper.insertPrice(symbol, price);
    }
}