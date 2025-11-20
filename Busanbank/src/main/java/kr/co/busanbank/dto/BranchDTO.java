package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지점 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private Integer branchId;      // 지점 ID (PK)
    private String branchName;     // 지점명
    private String branchCode;     // 지점코드
    private String regionCode;     // 지역코드
    private String address;        // 주소
    private String tel;            // 전화번호
    private String manager;        // 지점장
    private String status;         // 상태 (Y/N)
    private String createdAt;      // 생성일시
    private String updatedAt;      // 수정일시
}