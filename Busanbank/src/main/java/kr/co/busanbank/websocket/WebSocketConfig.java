package kr.co.busanbank.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final RankingWebSocketHandler rankingWebSocketHandler;
    private final PointNotificationWebSocketHandler pointNotificationWebSocketHandler;
    private final CallAgentWebSocketHandler callAgentWebSocketHandler;
    private final CallCustomerWebSocketHandler callCustomerWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");

        registry.addHandler(callAgentWebSocketHandler, "/ws/call-agent")
                .setAllowedOrigins("*");

        registry.addHandler(callCustomerWebSocketHandler, "/ws/call-customer")
                .setAllowedOrigins("*");

        registry.addHandler(rankingWebSocketHandler, "/ws/ranking")
                .setAllowedOrigins("*");

        // 포인트 알림 WebSocket (작성자: 진원, 2025-12-04)
        registry.addHandler(pointNotificationWebSocketHandler, "/ws/point-notification")
                .setAllowedOrigins("*");
    }
}
