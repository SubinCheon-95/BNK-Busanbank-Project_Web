/*
    날짜 : 2025/12/29
    이름 : 진원
    내용 : 거래내역 DTO 생성 - Flutter 앱 연동용
 */
package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryDTO {
    private Long transactionId;          // 거래 ID (시퀀스)
    private String fromAccountNo;        // 출금 계좌번호
    private String toAccountNo;          // 입금 계좌번호
    private Long amount;                 // 거래 금액
    private Long balanceAfter;           // 거래 후 잔액
    private String transactionType;      // 거래 구분 (TRANSFER:이체, DEPOSIT:입금, WITHDRAW:출금)
    private String transactionDate;      // 거래일시
    private String description;          // 거래 메모/설명
    private int userId;                  // 거래 사용자 ID

    // 조회용 추가 필드
    private String fromUserName;         // 출금자 이름
    private String toUserName;           // 입금자 이름
    private String productName;          // 상품명
}
