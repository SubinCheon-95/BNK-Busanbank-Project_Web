/*
    날짜 : 2025/12/29
    이름 : 진원
    내용 : 계좌 정보 DTO - Flutter 앱 연동용
 */
package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String accountNo;        // 계좌번호
    private Integer userId;          // 사용자 ID
    private Long balance;            // 잔액
    private String accountType;      // 계좌 타입
    private String createdAt;        // 생성일
}
