package kr.co.busanbank.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** ************************************
 *  Product 엔티티
************************************ */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** 상품 번호 (PK) */
    private int productNo;

    /** 상품명 */
    private String productName;

    /** 상품 타입 (01: 예금, 02: 적금) */
    private String productType;

    /** 카테고리 ID (FK) */
    private int categoryId;

    /** 상품 설명 */
    private String description;

    /** 기본 이율(%) */
    private BigDecimal baseRate;

    /** 만기 이율(%) */
    private BigDecimal maturityRate;

    /** 중도해지 이율(%) */
    private BigDecimal earlyTerminateRate;

    /** 월 납입액 (적금용) */
    private BigDecimal monthlyAmount;

    /** 저축 기간(개월) (적금용) */
    private int savingTerm;

    /** 예치 금액 (예금용) */
    private BigDecimal depositAmount;

    /** 이자 지급 방식 (단리/복리/만기일시/월복리) */
    private String interestMethod;

    /** 지급 주기 */
    private String payCycle;

    /** 판매 종료일 */
    private LocalDate endDate;

    /** 관리자 ID (FK) */
    private int adminId;

    /** 생성일시 */
    private LocalDateTime createdAt;

    /** 수정일시 */
    private LocalDateTime updatedAt;

    /** 상태 (Y: 활성, N: 비활성) */
    private String status;
}