package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 레벨 설정 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LevelSettingDTO {
    private Integer levelId;
    private Integer levelNumber;
    private String levelName;
    private Integer requiredPoints;
    private String levelIcon;
    private String levelDescription;
    private Date createdAt;
}
