package kr.co.busanbank.controller.admin;

import kr.co.busanbank.service.CoinApiService;
import kr.co.busanbank.service.CoinCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coin")
public class CoinApiController {

    private final CoinApiService coinApiService;
    private final CoinCacheService coinCacheService;

    public CoinApiController(CoinApiService coinApiService, CoinCacheService coinCacheService) {
        this.coinApiService = coinApiService;
        this.coinCacheService = coinCacheService;
    }

    @GetMapping("/latest")
    public List<Map<String,Object>> getLatestCrypto(@RequestParam(defaultValue = "BTC") String symbol) {
        List<Map<String,Object>> data = coinCacheService.getLatest();
        return data.stream()
                .filter(c -> c.get("symbol").equals(symbol))
                .toList();
    }
}