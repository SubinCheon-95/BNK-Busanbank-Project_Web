package kr.co.busanbank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqDTO {
    private int faqId;
    private String groupCode;
    private String faqCategory;
    private String question;
    private String answer;
    private String createDat;
    private String updateDat;
    private String status;

    //codeDetail 질문의 유형
    private String codeName;
}
