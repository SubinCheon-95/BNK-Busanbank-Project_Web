package kr.co.busanbank.service.chatting;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chatting.ChatSessionDTO;
import kr.co.busanbank.mapper.ChatSessionMapper;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final CsService csService;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UsersDTO getUserByLoginId(String loginId) throws Exception {
        return csService.getUserById(loginId);
    }

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
        String now = LocalDateTime.now().format(dtf);
        return chatSessionMapper.updateChatSessionStatus(sessionId, status, now);
    }

    public List<ChatSessionDTO> getWaitingSessions() {
        return chatSessionMapper.selectByStatus("WAITING");
    }

    public List<ChatSessionDTO> getChattingSessions(int consultantId) {
        return chatSessionMapper.selectByStatusAndConsultant("CHATTING", consultantId);
    }

    // 상담원 배정
    public int assignConsultant(int sessionId, int consultantId) {
        String now = LocalDateTime.now().format(dtf);

        return chatSessionMapper.assignConsultantToSession(
                sessionId,
                consultantId,
                "CHATTING"
        );
    }

    public int closeSession(int sessionId) {
        String now = LocalDateTime.now().format(dtf);

        return chatSessionMapper.closeChatSession(
                sessionId,
                "CLOSED",
                now,
                now
        );
    }
}
