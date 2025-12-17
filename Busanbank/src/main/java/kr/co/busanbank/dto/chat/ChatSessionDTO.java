package kr.co.busanbank.dto.chat;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionDTO {

    private Integer sessionId; //int -> integer
    private Integer userId;    //int -> integer
    private int consultantId;
    private String inquiryType;
    private String status;
    private int priorityScore;
    private String createdAt;
    private String updatedAt;
    private String waitStartTime;
    private String chatStartTime;
    private String chatEndTime;

    // 추가 필드 - 안 읽은 메시지 개수
    private Integer unreadCount;

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }


}
