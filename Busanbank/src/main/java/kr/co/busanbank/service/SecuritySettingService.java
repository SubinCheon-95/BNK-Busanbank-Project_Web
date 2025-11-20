package kr.co.busanbank.service;

import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.mapper.SecuritySettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-20
 * 수정일: 2025-11-20 (SSL 인증서 만료일 체크 추가)
 * 설명: 보안 설정 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SecuritySettingService {

    private final SecuritySettingMapper securitySettingMapper;

    /**
     * 모든 보안 설정 조회
     */
    public List<SecuritySettingDTO> getAllSettings() {
        return securitySettingMapper.selectAllSettings();
    }

    /**
     * 특정 보안 설정 조회
     */
    public SecuritySettingDTO getSettingByKey(String settingkey) {
        return securitySettingMapper.selectSettingByKey(settingkey);
    }

    /**
     * 보안 설정 수정
     */
    public boolean updateSetting(SecuritySettingDTO securitySettingDTO) {
        try {
            int result = securitySettingMapper.updateSetting(securitySettingDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("보안 설정 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * SSL 인증서 만료일까지 남은 일수 계산
     * 작성자: 진원, 2025-11-20
     * @return 남은 일수 (음수면 이미 만료됨)
     */
    public long getSSLCertExpiryDays() {
        try {
            SecuritySettingDTO setting = getSettingByKey("SSL_CERT_EXPIRY");
            if (setting == null || setting.getSettingvalue() == null) {
                return -1;
            }

            LocalDate expiryDate = LocalDate.parse(setting.getSettingvalue(), DateTimeFormatter.ISO_DATE);
            LocalDate today = LocalDate.now();

            return ChronoUnit.DAYS.between(today, expiryDate);
        } catch (Exception e) {
            log.error("SSL 인증서 만료일 계산 실패: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * SSL 인증서 만료 경고가 필요한지 확인
     * 작성자: 진원, 2025-11-20
     * @return 30일 이내 만료 또는 이미 만료된 경우 true
     */
    public boolean isSSLCertExpiringSoon() {
        long daysLeft = getSSLCertExpiryDays();
        return daysLeft >= 0 && daysLeft <= 30;
    }
}
