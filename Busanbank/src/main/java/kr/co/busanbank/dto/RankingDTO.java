package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 랭킹 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingDTO {
    private Integer rank;
    private Integer userId;
    private String userName;
    private Integer points;           // 총 포인트 또는 월별 포인트
    private Integer userLevel;
    private String levelName;
    private String profileImage;
}
