package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 사용자 포인트 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPointDTO {
    private Integer userPointId;
    private Integer userId;
    private Integer totalEarned;      // 총 획득 포인트
    private Integer currentPoint;     // 현재 보유 포인트
    private Integer totalUsed;        // 총 사용 포인트
    private Integer userLevel;        // 사용자 레벨
    private Date createdAt;
    private Date updatedAt;

    // 조인 데이터
    private String userName;
    private String levelName;
    private String levelIcon;         // 레벨 아이콘
    private String levelDescription;
    private Integer requiredPoints;   // 다음 레벨까지 필요한 포인트
}
