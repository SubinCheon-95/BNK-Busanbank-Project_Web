package kr.co.busanbank.dto.chat;

import lombok.Data;

/*
    이름 : 우지희
    날짜 :
    내용 : flutter JSON 응답 반환용 DTO
 */

@Data
public class ChatStartResponse {
    private Integer sessionId;
    private String status; // SUCCESS, FAIL
    private String message; // 안내 메시지
}
