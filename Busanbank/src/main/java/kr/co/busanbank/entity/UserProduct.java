package kr.co.busanbank.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** ****************************************
 *  UserProduct 엔티티
 *************************************** */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProduct {

    /** 회원 ID (PK, FK) */
    private int userId;

    /** 상품 번호 (PK, FK) */
    private int productNo;

    /** 상품 가입일 (PK) */
    private LocalDate startDate;

    /** 상태 (A: 유효, N: 해지) */
    private String status;

    /** 생성일시 */
    private LocalDateTime createdAt;

    /** 수정일시 */
    private LocalDateTime updatedAt;

    /** 상품 해지(종료)일 */
    private LocalDate endDate;

    /** 가입시 최종 적용 이율(%) */
    private BigDecimal applyRate;

    /** 계약 기간(개월): 만기일 계산 및 계약 유효성 검증용 */
    private int contractTerm;

    /** 가입 원금/최초 납입액: 이자 계산 기준 금액 */
    private BigDecimal principalAmount;

    /** 예상 만기일: startDate + contractTerm을 기준으로 계산 */
    private LocalDate expectedEndDate;

    /** 고객별 확정된 중도해지 이율(계약시점 값 복사) */
    private BigDecimal contractEarlyRate;

    /** 회원 계좌 비밀번호 */
    private String accountPassword;
}
