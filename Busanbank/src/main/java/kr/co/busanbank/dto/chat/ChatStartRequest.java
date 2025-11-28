package kr.co.busanbank.dto.chat;

import lombok.Data;

/*
    이름 : 우지희
    날짜 :
    내용 : REST로 세션 생성할 때 쓰는 DTO
 */

@Data
public class ChatStartRequest {

    private Integer userId;
    private String inquiryType;
}
