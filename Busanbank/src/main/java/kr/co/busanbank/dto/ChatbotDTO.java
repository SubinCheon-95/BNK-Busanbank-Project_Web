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


}
