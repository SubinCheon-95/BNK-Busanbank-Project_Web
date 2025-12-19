package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.chat.ChatMessageHistoryItem;
import kr.co.busanbank.dto.chat.ChatSessionHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface  ChatHistoryMapper {

    List<ChatSessionHistoryItem> selectSessionHistory(
            @Param("userId") int userId,
            @Param("cursor") String cursor,
            @Param("size") int size
    );

    List<ChatMessageHistoryItem> selectMessageHistory(
            @Param("userId") int userId,
            @Param("sessionId") int sessionId,
            @Param("cursor") String cursor,
            @Param("size") int size
    );
}
