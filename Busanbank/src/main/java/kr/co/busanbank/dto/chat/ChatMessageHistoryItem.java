package kr.co.busanbank.dto.chat;

import lombok.Data;

@Data
public class ChatMessageHistoryItem {
    private Integer messageId;
    private String senderType;
    private String message;   // messageText -> message
    private String createdAt;
}
