package kr.co.busanbank.dto;

import lombok.*;

/**
 * 보안 설정 DTO
 * 작성자: 진원
 * 날짜: 2025-11-20
 * 내용: 관리자 보안 설정 관리 (비밀번호 정책, 세션 타임아웃 등)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecuritySettingDTO {
    private String settingkey;
    private String settingvalue;
    private String settingtype;
    private String settingdesc;
    private String updateddate;
    private String updatedby;
    private String useyn;
}
