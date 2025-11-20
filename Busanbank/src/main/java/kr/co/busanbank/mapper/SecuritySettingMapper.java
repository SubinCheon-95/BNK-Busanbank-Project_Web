package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.SecuritySettingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-20
 * 설명: 보안 설정 Mapper 인터페이스
 */
@Mapper
public interface SecuritySettingMapper {

    // 모든 보안 설정 조회
    List<SecuritySettingDTO> selectAllSettings();

    // 특정 설정 조회
    SecuritySettingDTO selectSettingByKey(@Param("settingkey") String settingkey);

    // 설정 수정
    int updateSetting(SecuritySettingDTO securitySettingDTO);
}
