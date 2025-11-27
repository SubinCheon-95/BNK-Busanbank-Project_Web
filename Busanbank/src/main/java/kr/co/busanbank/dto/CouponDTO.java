package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 쿠폰 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponDTO {
    private Integer couponId;
    private String couponCode;
    private String couponName;
    private String description;
    private Double rateIncrease;
    private Integer maxUsageCount;
    private Integer currentUsageCount;
    private Date validFrom;
    private String validFromStr;
    private Date validTo;
    private String validToStr;
    private String isActive;
    private Integer adminId;
    private Date createdAt;
    private Date updatedAt;
    private String status;

    // 조인 데이터
    private List<Integer> categoryIds;
    private String categoryNames;
    private Integer availableCount;
}
