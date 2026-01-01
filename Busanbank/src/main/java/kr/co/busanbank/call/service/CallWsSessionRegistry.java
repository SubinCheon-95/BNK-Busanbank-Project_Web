package kr.co.busanbank.call.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallWsSessionRegistry {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void put(String consultantId, WebSocketSession session) {
        sessions.put(consultantId, session);
    }

    public void remove(String consultantId) {
        sessions.remove(consultantId);
    }

    public WebSocketSession get(String consultantId) {
        return sessions.get(consultantId);
    }
}
