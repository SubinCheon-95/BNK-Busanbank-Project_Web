package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountDTO {
    private int userId;
    private long accountNo;
    private int productNo;
    private String startDate;
    private String createdAt;

    // 조회용 컬럼
    private int balance;
    private String productName;
    private String expectedEndDate;
    private int applyRate;

}
