/*
    날짜 : 2025/11/26
    이름 : 오서정
    내용 : 챗봇 dto 작성
 */
package kr.co.busanbank.dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotDTO {
    private int id;
    private String category;
    private String keyword;
    private String content;
    private String createdAt;
    // 2025/12/19 - flutter 연동 기능 추가 - 작성자: 오서정
    private String actionCode;


}
