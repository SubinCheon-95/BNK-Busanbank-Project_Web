package kr.co.busanbank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoinApiService {

    @Value("${coin.api.key}")
    private String apiKey;

    @Value("${gold.api.key}")
    private String goldApiKey;

    private final RestTemplate restTemplate;

    public CoinApiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // 코인 가격 가져오기
    public String getLatestCrypto(String symbol) {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CMC_PRO_API_KEY", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }

    public List<Map<String, Object>> fetchFromCMC() {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CMC_PRO_API_KEY", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        Map<String,Object> body = response.getBody();
        return (List<Map<String,Object>>) body.get("data");  // data 배열 반환
    }

    // 금 시세 가져오기
    public Map<String, Double> fetchMetalPrice() {
        String goldUrl = "https://api.metalpriceapi.com/v1/latest";
        String url = goldUrl + "?api_key=" + goldApiKey + "&base=USD&currencies=XAU";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody();

        Map<String, Double> rates = new HashMap<>();
        Map<String, Object> rateMap = (Map<String, Object>) body.get("rates");

        rates.put("XAU", ((Number) rateMap.get("XAU")).doubleValue());         // 무게 환산값
        rates.put("USDXAU", ((Number) rateMap.get("USDXAU")).doubleValue());   // 금 가격 (USD)

        return rates;
    }

    //기름 시세 가져오기
    public Map<String, Object> fetchOilPrice() {
        String url = "https://api.oilpriceapi.com/v1/prices/latest";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token b9c27998219ddb9f84305a56da3a845352547f84500d19f879e4e1905f927634");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}