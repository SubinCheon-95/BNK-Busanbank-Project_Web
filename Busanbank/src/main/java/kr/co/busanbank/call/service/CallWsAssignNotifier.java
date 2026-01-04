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

    // =========================
    // 1) 기존: 특정 상담사에게 배정 이벤트
    // =========================
    @Override
    public void notifyAssigned(String sessionId, String consultantId, String agoraChannel) {
        sendTo(consultantId, new AssignedEvent(
                "CALL_ASSIGNED",
                sessionId,
                consultantId,
                agoraChannel,
                Instant.now().toEpochMilli()
        ));
    }

    // =========================
    // 2) ✅ 추가: enqueue 알림(전체 상담사)
    // =========================
    public void notifyEnqueued(String voiceSessionId) {
        broadcast(new VoiceEvent(
                "VOICE_ENQUEUED",
                voiceSessionId,
                null,
                null,
                Instant.now().toEpochMilli()
        ));
    }

    // =========================
    // 3) ✅ 추가: accept 알림(전체 상담사 or 필요시 특정 상담사)
    // =========================
    public void notifyAccepted(String voiceSessionId, String consultantId) {
        broadcast(new VoiceEvent(
                "VOICE_ACCEPTED",
                voiceSessionId,
                consultantId,
                null,
                Instant.now().toEpochMilli()
        ));
    }

    // =========================
    // 4) ✅ 추가: end 알림(전체 상담사)
    // =========================
    public void notifyEnded(String voiceSessionId, String by) {
        broadcast(new VoiceEvent(
                "VOICE_ENDED",
                voiceSessionId,
                by,
                null,
                Instant.now().toEpochMilli()
        ));
    }

    // =========================
    // 내부 공통 전송 유틸
    // =========================
    private void sendTo(String consultantId, Object event) {
        WebSocketSession ws = registry.get(consultantId);
        if (ws == null || !ws.isOpen()) {
            log.warn("⚠️ agent ws not connected. consultantId={}", consultantId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            ws.sendMessage(new TextMessage(json));
            log.info("📣 ws push. consultantId={}, type={}", consultantId, getType(event));
        } catch (Exception e) {
            log.error("❌ ws push failed. consultantId={}", consultantId, e);
        }
    }

    private void broadcast(Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            TextMessage tm = new TextMessage(json);

            registry.forEachSession(ws -> {
                try {
                    if (ws != null && ws.isOpen()) ws.sendMessage(tm);
                } catch (Exception e) {
                    log.warn("❌ ws broadcast send fail. wsSessionId={}", ws.getId(), e);
                }
            });

            log.info("📣 ws broadcast. type={}", getType(event));
        } catch (Exception e) {
            log.error("❌ ws broadcast failed", e);
        }
    }

    private String getType(Object event) {
        try {
            return (String) event.getClass().getMethod("getType").invoke(event);
        } catch (Exception ignore) {
            return event.getClass().getSimpleName();
        }
    }

    // =========================
    // DTOs
    // =========================
    @Data
    @AllArgsConstructor
    public static class AssignedEvent {
        private String type;          // "CALL_ASSIGNED"
        private String sessionId;
        private String consultantId;
        private String agoraChannel;
        private long assignedAt;
    }

    @Data
    @AllArgsConstructor
    public static class VoiceEvent {
        private String type;          // "VOICE_ENQUEUED" / "VOICE_ACCEPTED" / "VOICE_ENDED"
        private String voiceSessionId;
        private String consultantId;  // accept/end 주체(또는 수락한 상담사)
        private String extra;         // 필요하면 확장
        private long at;
    }
}
