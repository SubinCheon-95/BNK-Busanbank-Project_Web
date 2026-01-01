package kr.co.busanbank.websocket;

import kr.co.busanbank.call.service.CallAgentAvailabilityService;
import kr.co.busanbank.call.service.CallWsSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Slf4j
@Component
public class CallAgentWebSocketHandler extends TextWebSocketHandler {

    private final CallWsSessionRegistry registry;
    private final CallAgentAvailabilityService availabilityService;

    public CallAgentWebSocketHandler(CallWsSessionRegistry registry,
                                     CallAgentAvailabilityService availabilityService) {
        this.registry = registry;
        this.availabilityService = availabilityService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String consultantId = extractQueryParam(session, "consultantId");
        if (consultantId == null || consultantId.isBlank()) {
            closeQuietly(session, CloseStatus.BAD_DATA);
            return;
        }

        registry.put(consultantId, session);

        // ✅ WS 연결되면 READY 등록
        availabilityService.markReady(consultantId);

        log.info("✅ CallAgent WS connected. consultantId={}, sessionId={}", consultantId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String consultantId = extractQueryParam(session, "consultantId");
        if (consultantId != null && !consultantId.isBlank()) {
            registry.remove(consultantId);

            // ✅ WS 끊기면 OFFLINE 처리(READY 해제)
            availabilityService.markOffline(consultantId);
        }

        log.info("🔌 CallAgent WS closed. consultantId={}, sessionId={}, status={}",
                consultantId, session.getId(), status);
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
        try {
            session.close(status);
        } catch (Exception ignored) {}
    }
}
