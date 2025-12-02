package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 사용자 쿠폰 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCouponDTO {
    private Integer userCouponId;
    private Integer userId;
    private Integer couponId;
    private Date issuedDate;
    private Date usedDate;
    private Integer productNo;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private char eventParticipated;

    // 조인 데이터 (COUPON 테이블)
    private String couponCode;
    private String couponName;
    private String description;
    private Double rateIncrease;
    private Integer maxUsageCount;
    private Integer currentUsageCount;
    private Date validFrom;
    private Date validTo;
    private String isActive;
    private String eventCheck;

    // 조인 데이터 (기타)
    private String userName;
    private String productName;
}
