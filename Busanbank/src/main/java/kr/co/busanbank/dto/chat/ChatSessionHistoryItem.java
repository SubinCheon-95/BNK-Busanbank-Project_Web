package kr.co.busanbank.dto.chat;

import lombok.Data;

@Data
public class ChatSessionHistoryItem {
    private Integer sessionId;
    private String inquiryType;
    private String status;
    private String startedAt;     // createdAt
    private String endedAt;       // chatEndTime
    private String lastMessage;
    private String lastMessageAt;
}
