package kr.co.busanbank.dto;

import lombok.*;

/** *************************************
 *            ProductTermsDTO
 **************************************** */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTermsDTO {

    private int termId;              // 약관 ID
    private int productNo;           // 상품번호
    private String termType;         // 약관 유형 (ESSENTIAL/OPTIONAL)
    private String termTitle;        // 약관 제목
    private String termContent;      // 약관 내용
    private String isRequired;       // 필수여부 (Y/N)
    private int displayOrder;        // 표시 순서
    private String createdAt;        // 생성일
    private String updatedAt;        // 수정일
    private String status;           // 상태 (Y/N)

    // 추가 필드 (화면 표시용)
    private boolean agreed;          // 사용자 동의 여부 (화면에서 사용)
}