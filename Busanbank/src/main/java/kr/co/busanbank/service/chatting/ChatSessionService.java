package kr.co.busanbank.service;

import kr.co.busanbank.dto.ChatSessionDTO;
import kr.co.busanbank.dto.ConsultantDTO;
import kr.co.busanbank.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 세션 생성
    public ChatSessionDTO createChatSession(Integer userId, String inquiryType) {

        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setUserId(userId);
        dto.setInquiryType(inquiryType);
        dto.setStatus("WAITING");
        dto.setPriorityScore(0);

        chatSessionMapper.insertChatSession(dto);
        return dto;
    }

    // 세션 조회
    public ChatSessionDTO getChatSession(int sessionId) {
        return chatSessionMapper.selectChatSessionById(sessionId);
    }

    // 상태 변경
    public int updateStatus(int sessionId, String status) {
        return chatSessionMapper.updateChatSessionStatus(sessionId, status);
    }

    // 상담원 배정
    public int assignConsultant(int sessionId, int consultantId) {
        String now = LocalDateTime.now().format(dtf);

        return chatSessionMapper.assignConsultantToSession(
                sessionId,
                consultantId,
                "CHATTING",
                now
        );
    }
}
