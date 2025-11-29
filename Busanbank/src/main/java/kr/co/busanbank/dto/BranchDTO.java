package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작성자: 진원
 * 작성일: 2025-11-29
 * 설명: 지점 정보 DTO - 지도 기능 추가 필드 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private Integer branchId;       // 지점 ID (PK)
    private String branchName;      // 지점명
    private String branchCode;      // 지점코드
    private String regionCode;      // 지역코드
    private String address;         // 주소
    private String tel;             // 전화번호
    private String fax;             // 팩스번호
    private String manager;         // 지점장
    private String status;          // 상태 (Y/N)
    private String openDate;        // 개점일
    private Double latitude;        // 위도
    private Double longitude;       // 경도
    private String has365Corner;    // 365코너 여부 (Y/N)
    private String hasForeignExchange;  // 외국환 취급 여부 (Y/N)
    private String hasSafebox;      // 대여금고 여부 (Y/N)
    private String hasNightSafebox; // 야간금고 여부 (Y/N)
    private String createdAt;       // 생성일시
    private String updatedAt;       // 수정일시
    private Integer employeeCount;  // 직원 수 (조회 시에만 사용)
}