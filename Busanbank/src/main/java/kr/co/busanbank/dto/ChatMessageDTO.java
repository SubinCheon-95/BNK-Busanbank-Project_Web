package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private int messageId;
    private int sessionId;
    private String senderType;
    private int senderId;
    private String messageText;
    private int isRead;
    private String createdAt;
    private String updatedAt;

}
