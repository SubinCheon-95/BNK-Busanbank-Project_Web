package kr.co.busanbank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 출석체크 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendanceDTO {
    private Integer attendanceId;
    private Integer userId;
    private Date attendanceDate;
    private Integer consecutiveDays;  // 연속 출석 일수
    private Integer earnedPoints;     // 획득 포인트
    private Date createdAt;

    // 조인 데이터
    private String userName;
}
