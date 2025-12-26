package kr.co.busanbank.mapper;


import kr.co.busanbank.dto.chat.ChatSessionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    // 신규 세션 생성 (대기열 진입 시)
    int insertChatSession(ChatSessionDTO chatSession);

    // 세션 기본 조회
    ChatSessionDTO selectChatSessionById(@Param("sessionId") int sessionId);

    // ✅ 진행중(이어하기) 세션 조회
    ChatSessionDTO selectActiveSessionByUserId(int userId);


    // 상태, 시간 필드 변경
    int updateChatSession(ChatSessionDTO chatSession);

    int updateChatSessionStatus(@Param("sessionId") int sessionId,
                                @Param("status") String status,
                                @Param("updatedAt") String updatedAt);

    // ✅ WAITING → 다른 상태 전용
    int updateStatusFromWaiting(@Param("sessionId") int sessionId,
                                @Param("status") String status);

    // ✅ 2) WAITING -> (consultantId + status=CHATTING) 배정(전용)
    int assignConsultantFromWaiting(
            @Param("sessionId") int sessionId,
            @Param("consultantId") int consultantId,
            @Param("status") String status
    );

    List<ChatSessionDTO> selectByStatus(@Param("status") String status);

    List<ChatSessionDTO> selectByStatusAndConsultant(@Param("status") String status,
                                                     @Param("consultantId") int consultantId);

    // 상담원 배정 시 : 상담원id, 상태, chatstarttime 갱신
    int assignConsultantToSession(@Param("sessionId") int sessionId,
                                  @Param("consultantId") int consultantId,
                                  @Param("status") String status);

    // 상담 종료 시 : 상태, chatendtime 갱신
    int closeChatSession(@Param("sessionId") int sessionId,
                         @Param("status") String status);

    // 읽지 않은 메시지 수
    List<ChatSessionDTO> selectChattingSessionsWithUnread(@Param("consultantId") int consultantId);

    // 시간이 지나면 세션 정리

    List<Integer> findOldWaitingSessionIds(@Param("minutes") int minutes);

    int autoCloseOldChattingSessions(@Param("minutes") int minutes);

    int deleteOldClosedSessions(@Param("days") int days);
}
