package kr.co.busanbank.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelProductDTO {
    private int productNo;
    private String productName;
    private String startDate;
    private String expectedEndDate;

    private String accountNo;   // 계좌번호

    private BigDecimal principalAmount;     // ① 해지원금
    private BigDecimal earlyInterest;       // ② 해지이자
    private BigDecimal maturityInterest;    // ③ 만기후이자
    private BigDecimal refundInterest;      // ④ 환입이자
    private BigDecimal taxAmount;           // ⑤ 세금

    private BigDecimal netPayment;          // 차감지급액(①+②+③-④-⑤)
    private BigDecimal finalAmount;         // 실입금금액(원금 + netPayment)
    private double applyRate;               // 적용 금리
    private double earlyTerminateRate;      // 조기해지 금리
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
}
