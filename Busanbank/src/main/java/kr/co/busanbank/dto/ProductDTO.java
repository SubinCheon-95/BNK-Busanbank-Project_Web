package kr.co.busanbank.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 설명: 금융상품 정보 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
  
    private int productNo;          // 상품번호
    private String productName;     // 상품명
    private String productType;     // 상품유형 (01: 예금, 02: 적금)
    private int categoryId;         // 카테고리 ID
    private String description;     // 상품설명

    // 금리 정보
    private BigDecimal baseRate;            // 기본금리 (%)
    private BigDecimal maturityRate;        // 만기우대 (%)
    private BigDecimal earlyTerminateRate;  // 중도해지우대 (%)

    // 적금 속성
    private BigDecimal monthlyAmount;   // 월 납입액
    private int savingTerm;             // 적립기간 (개월)

    // 예금 속성
    private BigDecimal depositAmount;   // 예치금

    // 공통 이자 정보
    private String interestMethod;  // 이자 계산 방법 (단리/복리/만기일시/월복리)
    private String payCycle;        // 이자 지급

    private String endDate;         // 상품 종료일
    private int adminId;            // 등록자 ID
    private String createdAt;       // 생성일
    private String updatedAt;       // 수정일
    private String status;          // 상태 (Y/N)
  
}