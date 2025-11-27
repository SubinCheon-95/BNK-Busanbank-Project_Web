package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 출석 보상 설정 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendanceRewardDTO {
    private Integer rewardId;
    private Integer consecutiveDays;
    private Integer rewardPoints;
    private String rewardDescription;
    private String isActive;
    private Date createdAt;
}
