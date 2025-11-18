package kr.co.busanbank.dto;

import lombok.*;

/**
 * 작성자: 진원
 * 작성일: 2025-11-18
 * 설명: 상품 카테고리 정보 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private int categoryId;         // 카테고리 ID
    private String categoryName;    // 카테고리명
    private String createdAt;       // 생성일
    private String updatedAt;       // 수정일
    private String status;          // 상태 (Y/N)
    private Integer parentId;       // 부모 카테고리 ID
  
}
