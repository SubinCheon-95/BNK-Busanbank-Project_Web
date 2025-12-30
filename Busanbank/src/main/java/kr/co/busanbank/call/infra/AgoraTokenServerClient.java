package kr.co.busanbank.call.infra;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Map;

@Component
public class AgoraTokenServerClient {

    private final RestClient restClient;

    public AgoraTokenServerClient(RestClient.Builder builder) {
        // baseUrl은 application.yml에서 주입받게 Config에서 세팅할 예정
        this.restClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> issueToken(String baseUrl, String sessionId, String role) {
        return RestClient.create(baseUrl)
                .post()
                .uri("/api/call/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "sessionId", sessionId,
                        "role", role
                ))
                .retrieve()
                .body(Map.class);
    }
}
