package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 포인트 히스토리 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointHistoryDTO {
    private Integer historyId;
    private Integer userId;
    private String pointType;         // EARN, USE
    private String pointSource;       // QUIZ, ATTENDANCE, PRODUCT, COUPON
    private Integer pointAmount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String description;
    private Integer referenceId;
    private Date createdAt;

    // 조인 데이터
    private String userName;
}
