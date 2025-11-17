package kr.co.busanbank.dto;

import lombok.*;

/**
 * 작성자: 진원
 * 작성일: 2025-11-17
 * 설명: 정책약관 정보 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermDTO {

    private int termNo;             // 약관 번호 (PK)
    private String groupCode;       // 약관 유형 그룹
    private String termType;        // 약관 유형 세부 코드
    private String termVersion;     // 약관 버전
    private String termTitle;       // 약관 제목
    private String termContent;     // 약관 내용 (CLOB)
    private String applyDate;       // 적용일
    private String createdAt;       // 생성일
    private String updatedAt;       // 수정일
    private String status;          // 상태 (Y/N)

    // 조인용 필드 (코드명 표시용)
    private String termTypeName;    // 약관 유형명 (예: 전자금융거래약관)
}
