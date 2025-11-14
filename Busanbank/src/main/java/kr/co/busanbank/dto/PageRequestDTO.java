package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageRequestDTO {

    @Builder.Default
    private int no = 1;

    @Builder.Default
    private int pg = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String cate = "free";

    private String searchType;
    private String keyword;

    private String csType;

    // 페이지 처리
    private int offset;

    public int getOffset() {
        return (pg - 1) * size;
    }
}