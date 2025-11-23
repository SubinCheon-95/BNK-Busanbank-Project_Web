package kr.co.busanbank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    @Value("${deepl.api-key}")
    private String apiKey;

    public List<String> translateBatch(List<String> texts, String targetLang) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 여러 텍스트를 한 번에 전송
        StringBuilder body = new StringBuilder();
        body.append("auth_key=").append(apiKey);
        body.append("&target_lang=").append(targetLang);

        for (String text : texts) {
            body.append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
        }

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api-free.deepl.com/v2/translate",
                request,
                Map.class
        );

        List<Map> translations = (List<Map>) response.getBody().get("translations");
        List<String> results = new ArrayList<>();
        for (Map t : translations) {
            results.add((String) t.get("text"));
        }
        return results;
    }
}
