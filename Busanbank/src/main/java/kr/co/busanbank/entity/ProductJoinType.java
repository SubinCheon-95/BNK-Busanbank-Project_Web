package kr.co.busanbank.entity;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJoinType {
    private int productNo;
    private String joinType;
}
