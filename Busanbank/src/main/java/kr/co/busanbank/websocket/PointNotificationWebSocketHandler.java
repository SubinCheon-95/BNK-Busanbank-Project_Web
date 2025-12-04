package kr.co.busanbank.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 작성자: 진원
 * 작성일: 2025-12-04
 * 설명: 포인트 알림 WebSocket 핸들러
 * - 로그인한 사용자에게 실시간 포인트 알림 전송
 * - userId별로 세션 관리
 */
@Slf4j
@Component
public class PointNotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // userId -> WebSocketSession 매핑
    private final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션에서 userId 추출 (쿼리 파라미터로 전달받음)
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            try {
                Integer userId = Integer.parseInt(query.substring(7));
                userSessions.put(userId, session);
                log.info("✅ 포인트 알림 WebSocket 연결 - userId: {}, sessionId: {}", userId, session.getId());
            } catch (NumberFormatException e) {
                log.warn("⚠️ 잘못된 userId 형식: {}", query);
                session.close();
            }
        } else {
            log.warn("⚠️ userId가 없는 연결 시도");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("📩 포인트 알림 WebSocket 메시지 수신 [{}]: {}", session.getId(), payload);

        // Ping/Pong 처리 (연결 유지)
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // userId 찾아서 제거
        Integer userIdToRemove = null;
        for (Map.Entry<Integer, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                userIdToRemove = entry.getKey();
                break;
            }
        }

        if (userIdToRemove != null) {
            userSessions.remove(userIdToRemove);
            log.info("❌ 포인트 알림 WebSocket 연결 종료 - userId: {}, sessionId: {}, status: {}",
                userIdToRemove, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("⚠️ 포인트 알림 WebSocket 오류 - sessionId: {}", session.getId(), exception);
    }

    /**
     * 특정 사용자에게 포인트 알림 전송
     * @param userId 사용자 ID
     * @param points 지급된 포인트
     * @param message 알림 메시지
     */
    public void sendPointNotification(Integer userId, Integer points, String message) {
        WebSocketSession session = userSessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> notification = Map.of(
                    "type", "POINT_EARNED",
                    "userId", userId,
                    "points", points,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
                );

                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));

                log.info("💰 포인트 알림 전송 성공 - userId: {}, points: {}점, message: {}",
                    userId, points, message);
            } catch (Exception e) {
                log.error("❌ 포인트 알림 전송 실패 - userId: {}", userId, e);
            }
        } else {
            log.debug("⚠️ WebSocket 세션 없음 또는 닫힘 - userId: {}", userId);
        }
    }

    /**
     * 현재 연결된 세션 수 조회
     */
    public int getConnectedSessionCount() {
        return userSessions.size();
    }

    /**
     * 특정 사용자가 연결되어 있는지 확인
     */
    public boolean isUserConnected(Integer userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}
