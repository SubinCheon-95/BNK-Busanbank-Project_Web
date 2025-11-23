package kr.co.busanbank.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.service.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingWebSocketHandler extends TextWebSocketHandler {

    private final QuizService quizService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("Ranking WebSocket connected: {}", session.getId());

        sendRankingToSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Ranking WebSocket message received [{}]: {}", session.getId(), payload);

        if ("refresh".equals(payload)) {
            sendRankingToSession(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("Ranking WebSocket closed: {}, status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Ranking WebSocket error: " + session.getId(), exception);
    }

    private void sendRankingToSession(WebSocketSession session) {
        try {
            List<Map<String, Object>> ranking = quizService.getTopRanking(10);
            String json = objectMapper.writeValueAsString(ranking);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to send ranking: " + session.getId(), e);
        }
    }

    public void broadcastRanking() {
        try {
            List<Map<String, Object>> ranking = quizService.getTopRanking(10);
            String json = objectMapper.writeValueAsString(ranking);
            TextMessage message = new TextMessage(json);

            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast ranking: " + session.getId(), e);
                }
            });

            log.info("Ranking broadcast completed: {} sessions", sessions.size());
        } catch (Exception e) {
            log.error("Ranking broadcast error", e);
        }
    }
}
