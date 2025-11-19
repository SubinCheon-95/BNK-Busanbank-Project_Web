package kr.co.busanbank.dto;

import lombok.*;

/**
 * 사이트 기본설정 DTO
 * 작성자: 진원
 * 날짜: 2025-11-19
 * 내용: 푸터 정보 등 사이트 기본 설정 관리
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingDTO {
    private String settingkey;
    private String settingvalue;
    private String settingdesc;
    private String updateddate;
    private String updatedby;
}
