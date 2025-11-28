package kr.co.busanbank.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import kr.co.busanbank.dto.chat.ChatSocketMessage;
import kr.co.busanbank.service.chat.ChatMessageQueueService;
import kr.co.busanbank.service.chat.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageQueueService  chatMessageQueueService;
    private final ChatSessionService chatSessionService;

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

        // 현재 시간 생성
        String now = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        // 1) ChatSocketMessage → ChatMessageDTO 변환
        ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                .sessionId(msg.getSessionId())
                .senderType(msg.getSenderType())
                .senderId(msg.getSenderId())
                .messageText(msg.getMessage())
                .isRead(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 2) DB 직접 저장 대신 Redis 큐에 적재
        try {
            chatMessageQueueService.enqueue(chatMessageDTO);
        } catch (Exception e) {
            log.error("채팅 메시지 큐 적재 실패", e);
            // 정책에 따라: 실패해도 브로드캐스트는 계속 할지 여부 결정 가능
        }

        // 3) 같은 채팅방에 브로드캐스트 (기존 그대로 유지)
        broadcast(msg.getSessionId(), msg);
    }

    private void handleEnd(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("END 메시지에 sessionId가 없습니다.");
            return;
        }

        log.info("상담 종료 요청 [{}]", msg.getSessionId());

        // 1) DB 세션 상태 종료 처리
        chatSessionService.closeSession(msg.getSessionId());

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