package kr.co.busanbank.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chatting.ChatSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/*
    이름 : 우지희
    날짜 :
    내용 : 채팅상담 웹소켓 핸들러
 */

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // sessionId → WebSocketSession 목록 (동일 채팅방 여러 클라이언트)
    private final Map<Integer, List<WebSocketSession>> sessionRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 연결됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        log.info("수신 메시지: {}", payload);

        // 1) JSON → 객체 변환
        ChatSocketMessage msg;
        try {
            msg = objectMapper.readValue(payload, ChatSocketMessage.class);
        } catch (Exception e) {
            log.error("메시지 파싱 오류", e);
            return;
        }

        if (msg.getType() == null) {
            log.warn("메시지 type이 비어있습니다. payload={}", payload);
            return;
        }

        switch (msg.getType()) {
            case "ENTER":
                handleEnter(session, msg);
                break;

            case "CHAT":
                handleChat(session, msg);
                break;

            case "END":
                handleEnd(session, msg);
                break;

            default:
                log.warn("알 수 없는 메시지 타입: {}", msg.getType());
        }
    }

    private void handleEnter(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("ENTER 메시지에 sessionId가 없습니다.");
            return;
        }

        sessionRoom.putIfAbsent(msg.getSessionId(), new CopyOnWriteArrayList<>());
        sessionRoom.get(msg.getSessionId()).add(session);

        log.info("세션 {} 채팅방 {} 입장", session.getId(), msg.getSessionId());

        // 안내 메시지 (SYSTEM)
        if ("USER".equalsIgnoreCase(msg.getSenderType())){
            ChatSocketMessage welcome = new ChatSocketMessage();
            welcome.setType("SYSTEM");
            welcome.setSessionId(msg.getSessionId());
            welcome.setMessage("상담이 시작되었습니다.");

            broadcast(msg.getSessionId(), welcome);
        }
    }

    private void handleChat(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("CHAT 메시지에 sessionId가 없습니다.");
            return;
        }

        log.info("채팅 [{}]: {}", msg.getSessionId(), msg.getMessage());

        // TODO: DB 저장 예정 (ChatMessageService.insertMessage())
        // chatMessageService.save(msg);

        // 같은 채팅방에 브로드캐스트
        broadcast(msg.getSessionId(), msg);
    }

    private void handleEnd(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("END 메시지에 sessionId가 없습니다.");
            return;
        }

        log.info("상담 종료 요청 [{}]", msg.getSessionId());

        ChatSocketMessage endMsg = new ChatSocketMessage();
        endMsg.setType("END");
        endMsg.setSessionId(msg.getSessionId());
        endMsg.setMessage("상담이 종료되었습니다.");

        broadcast(msg.getSessionId(), endMsg);

        // 더 이상 메시지가 오면 안 되므로 세션 목록 삭제
        sessionRoom.remove(msg.getSessionId());
    }

    private void broadcast(int sessionId, ChatSocketMessage msg) throws IOException {
        List<WebSocketSession> list = sessionRoom.get(sessionId);
        if (list == null || list.isEmpty()) {
            log.info("세션 {}에 연결된 클라이언트가 없습니다.", sessionId);
            return;
        }

        String json = objectMapper.writeValueAsString(msg);
        TextMessage textMessage = new TextMessage(json);

        for (WebSocketSession s : list) {
            if (!s.isOpen()) continue;
            try {
                s.sendMessage(textMessage);
            } catch (Exception e) {
                log.error("WebSocket 전송 중 오류(sessionId={}, wsSessionId={})", sessionId, s.getId(), e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 종료: {}, status={}", session.getId(), status);

        // 끊긴 세션을 모든 room에서 제거
        sessionRoom.forEach((roomId, list) -> {
            list.removeIf(s -> s.getId().equals(session.getId()));
        });

        // 필요하면 빈 room 정리
        sessionRoom.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
}