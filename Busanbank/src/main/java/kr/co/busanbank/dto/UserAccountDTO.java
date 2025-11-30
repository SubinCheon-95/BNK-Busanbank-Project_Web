/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 회원 계좌 정보 dto 수정 작성
 */
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
    private String accountNo; /* 2015/12/01 - long->String 변경 (조인 조회) - 작성자: 오서정*/
    private int productNo;
    private String startDate;
    private String createdAt;

    // 조회용 컬럼
    private int balance;
    private String productName;
    private String expectedEndDate;
    private int applyRate;

}
