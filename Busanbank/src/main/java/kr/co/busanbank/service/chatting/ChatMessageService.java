package kr.co.busanbank.service;

import kr.co.busanbank.dto.ChatMessageDTO;

import kr.co.busanbank.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 메시지 전송
    public ChatMessageDTO sendMessage(Integer sessionId,
                                      String senderType,
                                      Integer senderId,
                                      String messageText) {

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(sessionId);
        dto.setSenderType(senderType);
        dto.setSenderId(senderId);
        dto.setMessageText(messageText);
        dto.setIsRead(0);

        chatMessageMapper.insertChatMessage(dto);
        return dto;
    }

    // 메시지 조회
    public List<ChatMessageDTO> getMessageBySessionId(Integer sessionId) {
        return chatMessageMapper.selectChatMessageBySessionId(sessionId);
    }

    // 읽음 처리
    public int markMessageAsRead(Integer sessionId, Integer readerId) {
        String now = LocalDateTime.now().format(dtf);
        return chatMessageMapper.updateMessageReadBySession(sessionId, readerId, now);
    }
}
