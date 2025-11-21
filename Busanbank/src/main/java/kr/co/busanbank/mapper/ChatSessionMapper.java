package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ChatSessionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatSessionMapper {

    // 신규 세션 생성 (대기열 진입 시)
    int insertChatSession(ChatSessionDTO chatSession);

    // 세션 기본 조회
    ChatSessionDTO selectChatSessionById(@Param("chatSessionId") int chatSessionId);

    // 상태, 시간 필드 변경
    int updateChatSession(ChatSessionDTO chatSession);

    int updateChatSessionStatus(@Param("chatSessionId") int chatSessionId,
                                 @Param("status") String status);

    // 상담원 배정 시 : 상담원id, 상태, chatstarttime 갱신
    int assignConsultantToSession(@Param("chatSessionId") int chatSessionId,
                                   @Param("consultantId") int consultantId,
                                   @Param("status") String status,
                                   @Param("chatStartTime") String chatStartTime);

    // 상담 종료 시 : 상태, chatendtime 갱신
    int closeChatSession(@Param("chatSessionId") int chatSessionId,
                          @Param("status") String status,
                          @Param("chatEndTime") String chatEndTime);
}
