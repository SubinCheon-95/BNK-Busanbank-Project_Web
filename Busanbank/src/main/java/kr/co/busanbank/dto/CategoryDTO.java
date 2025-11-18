package kr.co.busanbank.dto;

import lombok.Data;

@Data
public class CategoryDTO {

    private int categoryId;
    private String categoryName;
    private Integer parentId;

}
