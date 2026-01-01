package kr.co.busanbank.call.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

@Slf4j
@Component
public class CallWsAssignNotifier implements CallAssignNotifier{

    private final CallWsSessionRegistry registry;
    private final ObjectMapper objectMapper;

    public CallWsAssignNotifier(CallWsSessionRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notifyAssigned(String sessionId, String consultantId, String agoraChannel) {
        WebSocketSession ws = registry.get(consultantId);
        if (ws == null || !ws.isOpen()) {
            log.warn("⚠️ agent ws not connected. cannot push assigned. consultantId={}, sessionId={}", consultantId, sessionId);
            return;
        }

        AssignedEvent event = new AssignedEvent(
                "CALL_ASSIGNED",
                sessionId,
                consultantId,
                agoraChannel,
                Instant.now().toEpochMilli()
        );

        try {
            String json = objectMapper.writeValueAsString(event);
            ws.sendMessage(new TextMessage(json));
            log.info("📣 assigned pushed(raw-ws). consultantId={}, sessionId={}", consultantId, sessionId);
        } catch (Exception e) {
            log.error("❌ push assigned failed. consultantId={}, sessionId={}", consultantId, sessionId, e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class AssignedEvent {
        private String type;          // "CALL_ASSIGNED"
        private String sessionId;
        private String consultantId;
        private String agoraChannel;
        private long assignedAt;
    }
}
