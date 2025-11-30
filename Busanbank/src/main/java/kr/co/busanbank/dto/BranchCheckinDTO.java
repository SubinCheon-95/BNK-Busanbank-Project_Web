package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작성자: 진원
 * 작성일: 2025-11-30
 * 설명: 영업점 체크인 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCheckinDTO {
    private Integer checkinId;        // 체크인 ID (PK)
    private Integer userId;           // 사용자 ID
    private Integer branchId;         // 영업점 ID
    private String checkinDate;       // 체크인 일시
    private Double latitude;          // 체크인 시 사용자 위도
    private Double longitude;         // 체크인 시 사용자 경도
    private Integer pointsReceived;   // 지급된 포인트

    // 조회 시 추가 정보
    private String branchName;        // 영업점명
    private String branchAddress;     // 영업점 주소
}
