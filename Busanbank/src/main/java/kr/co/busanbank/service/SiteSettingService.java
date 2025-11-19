package kr.co.busanbank.service;

import kr.co.busanbank.dto.SiteSettingDTO;
import kr.co.busanbank.mapper.SiteSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-19
 * 설명: 사이트 기본설정 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SiteSettingService {

    private final SiteSettingMapper siteSettingMapper;

    /**
     * 모든 설정 조회
     */
    public List<SiteSettingDTO> getAllSettings() {
        return siteSettingMapper.selectAllSettings();
    }

    /**
     * 특정 설정 조회
     */
    public SiteSettingDTO getSettingByKey(String settingkey) {
        return siteSettingMapper.selectSettingByKey(settingkey);
    }

    /**
     * 설정 수정
     */
    public boolean updateSetting(SiteSettingDTO siteSettingDTO) {
        try {
            int result = siteSettingMapper.updateSetting(siteSettingDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("설정 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Map 형태로 조회 (푸터에서 사용)
     * key-value 형태로 반환
     */
    public Map<String, String> getSettingsAsMap() {
        try {
            List<SiteSettingDTO> settings = siteSettingMapper.selectAllSettings();
            Map<String, String> settingsMap = new HashMap<>();

            for (SiteSettingDTO setting : settings) {
                settingsMap.put(setting.getSettingkey(), setting.getSettingvalue());
            }

            return settingsMap;
        } catch (Exception e) {
            log.error("설정 조회 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
