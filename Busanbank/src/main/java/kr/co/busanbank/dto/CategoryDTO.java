package kr.co.busanbank.dto;

import lombok.*;

/*
    수정일 : 2025/11/18
    수정자 : 장진원
    내용 : 상품 카테고리 필드 추가(추가 필드 참조)
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

    //추가 필드
    private  String routePath;
}
