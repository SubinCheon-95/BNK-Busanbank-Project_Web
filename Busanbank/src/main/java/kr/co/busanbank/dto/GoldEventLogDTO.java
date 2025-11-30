/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 금 이벤트 dto 작성
*/
package kr.co.busanbank.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldEventLogDTO {
    private int id;
    private Integer userNo;
    private Double todayPrice;
    private Double errorRate;
    private Double minPrice;
    private Double maxPrice;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime  resultAt;


}
