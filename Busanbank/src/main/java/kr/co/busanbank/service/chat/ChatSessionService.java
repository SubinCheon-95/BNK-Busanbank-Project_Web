package kr.co.busanbank.service.chat;

import kr.co.busanbank.domain.ChatSessionStatus;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.mapper.ChatMessageMapper;
import kr.co.busanbank.mapper.ChatSessionMapper;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    private final ChatWaitingQueueService chatWaitingQueueService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UsersDTO getUserByLoginId(String loginId) throws Exception {
        return csService.getUserById(loginId);
    }


    // 세션 생성 (priorityScore 파라미터 받는 버전)
    public ChatSessionDTO createChatSession(Integer userId, String inquiryType, int priorityScore) {

        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setUserId(userId);
        dto.setInquiryType(inquiryType);
        dto.setStatus("WAITING");
        dto.setPriorityScore(priorityScore);

        // 1) DB 저장
        chatSessionMapper.insertChatSession(dto);

        int sessionId = dto.getSessionId();

        // 2) Redis ZSet 대기열 등록
        chatWaitingQueueService.enqueue(sessionId, priorityScore);

        log.info("💬 새 세션 생성 - sessionId={}, userId={}, inquiryType={}, priorityScore={}",
                sessionId, userId, inquiryType, priorityScore);

        return dto;
    }
    // 세션 조회
    public ChatSessionDTO getChatSession(int sessionId) {
        return chatSessionMapper.selectChatSessionById(sessionId);
    }

    // sessionId별로 "welcome sent" 1회 보장
    public boolean markWelcomeSentIfFirst(int sessionId) {
        String key = "chat:welcomeSent:" + sessionId;
        // SETNX: 키가 없을 때만 set 성공(true)
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(6));
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 우선순위 점수 계산 로직
     */
    public int calcPriorityScore(String priorityLevel, String inquiryType) {

        // 1) 고객 등급: base 점수
        int base = switch (priorityLevel == null ? "BASIC" : priorityLevel.toUpperCase()) {
            case "VIP"      -> 100;
            case "STANDARD" -> 50;
            case "BASIC"    -> 10;
            default         -> 10;
        };

        // 2) 문의 유형별 가중치
        int typeBonus = switch (inquiryType) {
            case "대출" -> 30;
            case "카드" -> 20;
            case "예금" -> 15;
            case "분실" -> 50;
            case "상품" -> 40;
            case "상품 가입" -> 100;
            default -> 0;
        };

        return base + typeBonus;
    }

    // 상담 종료 처리
    public int closeSession(int sessionId) {
        int updated = chatSessionMapper.closeChatSession(sessionId, "CLOSED");

        // DB에서 실제로 닫힌 경우에만 정리(불필요한 delete 방지)
        if (updated > 0) {
            // ✅ waiting/assigning 어디에 있든 제거
            chatWaitingQueueService.removeEverywhere(sessionId);
            clearWelcomeSent(sessionId);
        }

        return updated;
    }

    // sessionId별 "welcome sent" 키 정리
    public void clearWelcomeSent(int sessionId) {
        stringRedisTemplate.delete("chat:welcomeSent:" + sessionId);
    }

    // 상태 변경
    public int updateStatus(int sessionId, String status) {
        String now = LocalDateTime.now().format(dtf);
        int updated = chatSessionMapper.updateChatSessionStatus(sessionId, status, now);

        if (updated > 0 && !"WAITING".equals(status)) {
            chatWaitingQueueService.removeEverywhere(sessionId);
        }
        return updated;
    }

    // ✅ WAITING -> (CHATTING/CLOSED/...) 전환 전용
    public int updateStatusFromWaiting(int sessionId, String status) {
        int updated = chatSessionMapper.updateStatusFromWaiting(sessionId, status);

        // ✅ DB에서 WAITING이었다가 바뀐 경우에만 Redis에서 제거
        if (updated > 0) {
            chatWaitingQueueService.removeEverywhere(sessionId);
        }
        return updated;
    }

    // ✅ WAITING -> 상담원 배정 전용 (consultantId + CHATTING) (Redis 정리 포함)
    public int assignConsultantFromWaiting(int sessionId, int consultantId) {
        int updated = chatSessionMapper.assignConsultantFromWaiting(sessionId, consultantId, "CHATTING");

        if (updated > 0) {
            chatWaitingQueueService.removeEverywhere(sessionId);
        }
        return updated;
    }

    public List<ChatSessionDTO> getWaitingSessions() {
        return chatSessionMapper.selectByStatus("WAITING");
    }

    public List<ChatSessionDTO> getChattingSessions(int consultantId) {
        return chatSessionMapper.selectChattingSessionsWithUnread(consultantId);
    }

    // 관리자 수동배정 시
    public int assignConsultantManually(int sessionId, int consultantId) {
        int updated = chatSessionMapper.assignConsultantToSession(sessionId, consultantId, "CHATTING");
        if (updated > 0) {
            chatWaitingQueueService.removeEverywhere(sessionId);
            clearWelcomeSent(sessionId);
        }
        return updated;
    }

    // 진행 중 세션 조회
    public ChatSessionDTO findOrCreateSession(int userId, String inquiryType, int priorityScore
    ) {
        // 1️⃣ 진행중 세션 있는지 먼저 확인
        ChatSessionDTO active =
                chatSessionMapper.selectActiveSessionByUserId(userId);

        if (active != null) {
            // 🔴 핵심: WAITING인데 Redis에 없을 수 있으니 무조건 보정
            if (ChatSessionStatus.WAITING.name().equals(active.getStatus())) {
                chatWaitingQueueService.enqueue(
                        active.getSessionId(),
                        active.getPriorityScore()
                );
                log.info("♻️ 기존 WAITING 세션 재-enqueue - sessionId={}", active.getSessionId());
            }
            return active;
        }

        // 2️⃣ 없으면 새 세션 생성
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setUserId(userId);
        dto.setInquiryType(inquiryType);
        dto.setStatus(ChatSessionStatus.WAITING.name());
        dto.setPriorityScore(priorityScore);

        // 1) DB 저장 (여기서 dto.sessionId 채워짐)
        chatSessionMapper.insertChatSession(dto);

        // 2) Redis ZSet 대기열 등록
        int sessionId = dto.getSessionId();
        chatWaitingQueueService.enqueue(sessionId, priorityScore);

        log.info("🆕 신규 채팅 세션 생성 + 대기열 등록 - sessionId={}, score={}", sessionId, priorityScore);
        return dto;
    }

    public ChatSessionDTO getActiveSession(int userId) {
        ChatSessionDTO active = chatSessionMapper.selectActiveSessionByUserId(userId);
        if (active != null) {
            log.info("🔎 진행중 세션 조회 - userId={}, sessionId={}, status={}",
                    userId, active.getSessionId(), active.getStatus());
        } else {
            log.info("🔎 진행중 세션 없음 - userId={}", userId);
        }
        return active;
    }
}

