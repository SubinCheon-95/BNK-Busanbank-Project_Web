package kr.co.busanbank.call.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class CallCustomerWsSessionRegistry {

    // key = voiceSessionId
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void put(String voiceSessionId, WebSocketSession session) {
        sessions.put(voiceSessionId, session);
    }

    public void remove(String voiceSessionId) {
        sessions.remove(voiceSessionId);
    }

    public WebSocketSession get(String voiceSessionId) {
        return sessions.get(voiceSessionId);
    }

    public void forEachSession(Consumer<WebSocketSession> consumer) {
        sessions.values().forEach(consumer);
    }
}
