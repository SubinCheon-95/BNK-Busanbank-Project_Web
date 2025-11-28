package kr.co.busanbank.service.chat;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.mapper.ChatMessageMapper;
import kr.co.busanbank.mapper.ChatSessionMapper;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final CsService csService;
    private final ChatMessageMapper chatMessageMapper;

    // 추가
    private final ChatWaitingQueueService chatWaitingQueueService;

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

        // 1) DB에 세션 저장
        chatSessionMapper.insertChatSession(dto);

        int sessionId = dto.getSessionId();

        // 2) Redis 대기열에 등록
        chatWaitingQueueService.enqueue(sessionId);

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
        return chatSessionMapper.selectChattingSessionsWithUnread(consultantId);
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

    /**
     * Redis 대기열에서 다음 세션을 꺼내 상담원에게 배정
     */
    public ChatSessionDTO assignNextWaitingSession(int consultantId) {

        while (true) {
        // 1) Redis 대기열에서 다음 세션 하나 가져오기
        Integer sessionId = chatWaitingQueueService.popNextSession();
        if (sessionId == null) {
            return null; // 대기중인 세션 없음
        }

        ChatSessionDTO session = chatSessionMapper.selectChatSessionById(sessionId);

        // 2) DB에 없거나, 이미 WAITING이 아닌 경우는 건너뛰고 다음 것 pop
        if (session == null || !"WAITING".equals(session.getStatus())) {
            log.info("⏭ 사용 불가 세션 skip - sessionId={}, session={}", sessionId, session);
            continue;
        }

        // 3) 상담원 배정 + 상태 CHATTING 으로 변경
        chatSessionMapper.assignConsultantToSession(
                sessionId,
                consultantId,
                "CHATTING"
        );

        log.info("👨‍💼 상담원 배정 - consultantId={}, sessionId={}", consultantId, sessionId);

        // 필요하면 session 객체에도 상담원/상태 반영해서 리턴
        session.setConsultantId(consultantId);
        session.setStatus("CHATTING");

        return session;
    }
}
    public int closeSession(int sessionId) {
        String now = LocalDateTime.now().format(dtf);

        return chatSessionMapper.closeChatSession(
                sessionId,
                "CLOSED"
        );
    }
}
