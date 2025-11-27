package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 쿠폰-카테고리 매핑 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponCategoryDTO {
    private Integer couponCategoryId;
    private Integer couponId;
    private Integer categoryId;
    private Date createdAt;

    // 조인 데이터
    private String categoryName;
}
