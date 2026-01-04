package kr.co.busanbank.dto.chat;

import lombok.Data;

import java.util.Map;

/*
    이름 : 우지희
    날짜 :
    내용 : WebSocket으로 실시간 메시지 주고받을 때 쓰는 DTO
 */

@Data
public class ChatSocketMessage {

    private String type;       // ENTER, CHAT, READ, END
    private Integer sessionId;
    private String senderType;
    private Integer senderId;
    private String message;

    // ✅ 추가
    private Boolean isTyping;

    // ✅ 추가
    private Map<String, Object> extra;

}
