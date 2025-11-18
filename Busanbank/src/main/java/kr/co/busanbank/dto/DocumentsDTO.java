package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentsDTO {
    private int docId;
    private String groupCode;
    private String docCategory;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private String status;

    //codeDetail 질문의 유형
    private String codeName;
}
