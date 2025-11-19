package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.SiteSettingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-19
 * 설명: 사이트 기본설정 Mapper 인터페이스
 */
@Mapper
public interface SiteSettingMapper {

    // 모든 설정 조회
    List<SiteSettingDTO> selectAllSettings();

    // 특정 설정 조회
    SiteSettingDTO selectSettingByKey(@Param("settingkey") String settingkey);

    // 설정 수정
    int updateSetting(SiteSettingDTO siteSettingDTO);

    // Map 형태로 조회 (key-value 쌍)
    Map<String, String> selectSettingsAsMap();
}
