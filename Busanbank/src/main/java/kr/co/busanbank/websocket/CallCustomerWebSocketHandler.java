package kr.co.busanbank.websocket;

import kr.co.busanbank.call.service.CallCustomerWsSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Slf4j
@Component
public class CallCustomerWebSocketHandler extends TextWebSocketHandler {

    private final CallCustomerWsSessionRegistry registry;

    public CallCustomerWebSocketHandler(CallCustomerWsSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String voiceSessionId = extractQueryParam(session, "voiceSessionId");
        if (voiceSessionId == null || voiceSessionId.isBlank()) {
            closeQuietly(session, CloseStatus.BAD_DATA);
            return;
        }

        registry.put(voiceSessionId, session);
        log.info("✅ CallCustomer WS connected. voiceSessionId={}, wsSessionId={}", voiceSessionId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String voiceSessionId = extractQueryParam(session, "voiceSessionId");
        if (voiceSessionId != null && !voiceSessionId.isBlank()) {
            registry.remove(voiceSessionId);
        }
        log.info("🔌 CallCustomer WS closed. voiceSessionId={}, wsSessionId={}, status={}",
                voiceSessionId, session.getId(), status);
    }

    private String extractQueryParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;

        String[] parts = uri.getQuery().split("&");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return kv[1];
        }
        return null;
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try { session.close(status); } catch (Exception ignored) {}
    }
}
