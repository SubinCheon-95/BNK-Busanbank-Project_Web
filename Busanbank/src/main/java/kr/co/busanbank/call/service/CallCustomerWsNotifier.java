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
public class CallCustomerWsNotifier {

    private final CallCustomerWsSessionRegistry registry;
    private final ObjectMapper objectMapper;

    public CallCustomerWsNotifier(CallCustomerWsSessionRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public void notifyAccepted(String voiceSessionId, String consultantId, String agoraChannel) {
        send(voiceSessionId, new VoiceEvent(
                "VOICE_ACCEPTED",
                voiceSessionId,
                consultantId,
                agoraChannel,
                Instant.now().toEpochMilli()
        ));
    }

    public void notifyEnded(String voiceSessionId, String by) {
        send(voiceSessionId, new VoiceEvent(
                "VOICE_ENDED",
                voiceSessionId,
                by,
                null,
                Instant.now().toEpochMilli()
        ));
    }

    private void send(String voiceSessionId, Object event) {
        WebSocketSession ws = registry.get(voiceSessionId);
        if (ws == null || !ws.isOpen()) {
            log.warn("⚠️ customer ws not connected. voiceSessionId={}", voiceSessionId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            ws.sendMessage(new TextMessage(json));
            log.info("📣 customer ws push. voiceSessionId={}, type={}", voiceSessionId, ((VoiceEvent) event).type);
        } catch (Exception e) {
            log.error("❌ customer ws push failed. voiceSessionId={}", voiceSessionId, e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class VoiceEvent {
        private String type;          // VOICE_ACCEPTED / VOICE_ENDED
        private String voiceSessionId;
        private String consultantId;  // accept: 상담사, end: 종료 주체
        private String agoraChannel;  // accept 때 채널명 내려주면 Flutter가 바로 join 가능
        private long at;
    }
}
