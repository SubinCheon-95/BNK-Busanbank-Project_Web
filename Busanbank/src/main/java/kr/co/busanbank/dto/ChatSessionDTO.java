package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionDTO {

    private int sessionId;
    private int userId;
    private int consultantId;
    private String inquiryType;
    private String status;
    private int priorityScore;
    private String createdAt;
    private String updatedAt;
    private String waitStartTime;
    private String chatStartTime;
    private String chatEndTime;

}
