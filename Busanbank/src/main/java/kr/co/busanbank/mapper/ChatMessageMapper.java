package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ChatMessageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    // 메세지 저장
    int insertChatMessage(ChatMessageDTO chatMessage);

    // 세션 기준 메시지 목록 조회
    List<ChatMessageDTO> selectChatMessageBySessionId(@Param("sessionId") int sessionId);

    // 읽음 처리
    // - sessionId : 어떤 채팅방, readerId : 이 채팅방 읽고 있는 사람 id
    int updateMessageReadBySession(@Param("sessionId") int sessionId,
                                    @Param("readerId") int readerId,
                                    @Param("updatedAt") String updateAt);
}
