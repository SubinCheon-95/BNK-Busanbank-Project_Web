package kr.co.busanbank.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMemberDTO {
    private long userNo;
    private String userName;
    private String userId;
    private String hp;
    private String role;
    private String regDate;
    private String updatedAt;
    private String status;
    //계좌
    private long accountNo;
    private BigDecimal balance;
}
