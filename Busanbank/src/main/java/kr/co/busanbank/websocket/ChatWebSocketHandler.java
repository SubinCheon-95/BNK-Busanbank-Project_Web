package kr.co.busanbank.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.dto.chat.ChatSocketMessage;
import kr.co.busanbank.service.chat.ChatMessageStreamProducer;
import kr.co.busanbank.service.chat.ChatSessionService;
import kr.co.busanbank.service.chat.ChatWaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
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

    private final ObjectMapper objectMapper;
    private final ChatMessageStreamProducer chatMessageStreamProducer;
    private final ChatSessionService chatSessionService;
    private final ChatWaitingQueueService chatWaitingQueueService;

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
            log.error("❌ 메시지 파싱 오류. payload={}", payload, e);
            return;
        }

        if (msg.getType() == null) {
            log.warn("⚠ 메시지 type이 비어있습니다. payload={}", payload);
            return;
        }

        switch (msg.getType()) {
            case "ENTER" -> handleEnter(session, msg);
            case "CHAT"  -> handleChat(session, msg);
            case "TYPING" -> broadcast(msg.getSessionId(), msg);
            case "END"   -> handleEnd(session, msg);
            default      -> log.warn("알 수 없는 메시지 타입: {}", msg.getType());
        }
    }

    private void handleEnter(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("ENTER 메시지에 sessionId가 없습니다.");
            return;
        }

        sessionRoom.putIfAbsent(msg.getSessionId(), new CopyOnWriteArrayList<>());
        if (!sessionRoom.get(msg.getSessionId()).contains(session)) { // ✅ 중복 방지
            sessionRoom.get(msg.getSessionId()).add(session);
        }

        log.info("세션 {} 채팅방 {} 입장", session.getId(), msg.getSessionId());

        // ✅ 안내 메시지 (SYSTEM) - 세션당 1번만
        if ("USER".equalsIgnoreCase(msg.getSenderType())) {

            boolean first = chatSessionService.markWelcomeSentIfFirst(msg.getSessionId());
            if (!first) return;

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

        sessionRoom.putIfAbsent(msg.getSessionId(), new CopyOnWriteArrayList<>());
        if (!sessionRoom.get(msg.getSessionId()).contains(session)) {
            sessionRoom.get(msg.getSessionId()).add(session);
        }

        log.info("채팅 [{}]: {}", msg.getSessionId(), msg.getMessage());

        // ==============================
        // 1️⃣ USER 메시지면 senderId를 세션의 userNo로 강제
        // ==============================
        if ("USER".equalsIgnoreCase(msg.getSenderType())) {
            ChatSessionDTO chatSession =
                    chatSessionService.getChatSession(msg.getSessionId());

            if (chatSession == null || chatSession.getUserId() == null) {
                log.warn("USER 메시지인데 세션에 userId(userNo)가 없습니다. sessionId={}",
                        msg.getSessionId()
                );
                return; // ✅ 비정상 접근 차단
            }
            // 🔥 핵심: senderId를 세션의 userNo(PK)로 덮어쓰기
            msg.setSenderId(chatSession.getUserId());
        }

        // ==============================
        // 2️⃣ 시간 생성
        // ==============================
        String now = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        // ==============================
        // 3️⃣ ChatSocketMessage → ChatMessageDTO
        // ==============================
        ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                .sessionId(msg.getSessionId())
                .senderType(msg.getSenderType())
                .senderId(msg.getSenderId())
                .messageText(msg.getMessage())
                .isRead(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // ==============================
        // 4️⃣ Redis 큐 적재
        // ==============================
        try {
            chatMessageStreamProducer.enqueue(chatMessageDTO);
            log.info("✅ Stream 적재 완료: sessionId={}, senderId={}",
                    msg.getSessionId(), msg.getSenderId());
        } catch (Exception e) {
            log.error("❌ Stream 적재 실패", e);
        }

        // ==============================
        // 5️⃣ 채팅방 브로드캐스트
        // ==============================
        broadcast(msg.getSessionId(), msg);
    }

    private void handleEnd(WebSocketSession session, ChatSocketMessage msg) throws IOException {
        if (msg.getSessionId() == null) {
            log.warn("END 메시지에 sessionId가 없습니다.");
            return;
        }

        log.info("상담 종료 요청 [{}]", msg.getSessionId());

        // ✅ DB + Redis 정리는 서비스에 위임 (여기서 끝)
        chatSessionService.closeSession(msg.getSessionId());

        // ✅ 종료 알림 브로드캐스트
        ChatSocketMessage endMsg = new ChatSocketMessage();
        endMsg.setType("END");
        endMsg.setSessionId(msg.getSessionId());
        endMsg.setMessage("상담이 종료되었습니다.");

        broadcast(msg.getSessionId(), endMsg);

        // ✅ WebSocket room 정리
        sessionRoom.remove(msg.getSessionId());
    }

    private void broadcast(int sessionId, ChatSocketMessage msg) throws IOException {
        List<WebSocketSession> list = sessionRoom.get(sessionId);
        if (list == null || list.isEmpty()) return;

        // ✅ 닫힌 소켓 제거
        list.removeIf(s -> !s.isOpen());

        if (list.isEmpty()) {
            sessionRoom.remove(sessionId);
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

    public void systemBroadcast(int sessionId, String text) {
        try {
            ChatSocketMessage m = new ChatSocketMessage();
            m.setType("SYSTEM");
            m.setSessionId(sessionId);
            m.setMessage(text);
            broadcast(sessionId, m); // 기존 private broadcast 사용
        } catch (Exception e) {
            log.error("SYSTEM broadcast fail. sessionId={}", sessionId, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 종료: {}, status={}", session.getId(), status);

        // 끊긴 세션을 모든 room에서 제거
        sessionRoom.forEach((roomId, list) -> list.removeIf(s -> s.getId().equals(session.getId())));

        // 필요하면 빈 room 정리
        sessionRoom.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
}