package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private Integer empId;         // 직원 ID (PK)
    private Integer branchId;      // 지점 ID (FK)
    private String empName;        // 직원명
    private String empCode;        // 직원코드
    private String position;       // 직급
    private String department;     // 부서
    private String email;          // 이메일
    private String tel;            // 전화번호
    private String hireDate;       // 입사일
    private String status;         // 상태 (Y/N)
    private String createdAt;      // 생성일시
    private String updatedAt;      // 수정일시

    // 추가 정보 (조인용)
    private String branchName;     // 지점명
    private String branchCode;     // 지점코드
}