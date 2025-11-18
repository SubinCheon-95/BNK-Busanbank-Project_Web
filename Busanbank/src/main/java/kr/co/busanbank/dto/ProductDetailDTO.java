package kr.co.busanbank.dto;

import lombok.*;

import java.math.BigDecimal;

/** *****************************************
 *  ProductDetail DTO - 수정본
 ****************************************** */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDTO {

    private int productNo;          // 상품번호
    private String eligibility;     // 가입자격
    private BigDecimal minAmount;   // ★★★ String에서 BigDecimal로 변경 ★★★
    private String depositType;     // 예금과목
    private String paymentMethod;   // 납입방법
    private String requiredDocs;    // 필요서류
    private String taxBenefit;      // 세제혜택
    private String paymentLimit;    // 원금 또는 이자지급제한
    private String interestPayment; // 이자지급방식
    private String premiumRateInfo; // 우대이율 정보
    private String productFeatures; // 상품특징
    private String notice;          // 유의사항
    private String imageUrl;        // 이미지 URL
    private String createdAt;       // 생성일시
    private String updatedAt;       // 수정일시
}