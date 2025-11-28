package kr.co.busanbank.service;

import kr.co.busanbank.dto.LevelSettingDTO;
import kr.co.busanbank.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 레벨 설정 서비스
 */
@Service
@RequiredArgsConstructor
public class LevelService {

    private final PointMapper pointMapper;

    /**
     * 모든 레벨 설정 조회
     */
    public List<LevelSettingDTO> getAllLevels() {
        return pointMapper.selectAllLevelSettings();
    }

    /**
     * 레벨 번호로 조회
     */
    public LevelSettingDTO getLevelByNumber(int levelNumber) {
        return pointMapper.selectLevelSettingByNumber(levelNumber);
    }

    /**
     * 총 포인트로 레벨 조회
     */
    public LevelSettingDTO getLevelByTotalPoints(int totalPoints) {
        return pointMapper.selectLevelByTotalPoints(totalPoints);
    }

    /**
     * 레벨 설정 추가 (관리자)
     */
    @Transactional
    public boolean addLevel(LevelSettingDTO levelDTO) {
        try {
            pointMapper.insertLevelSetting(levelDTO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 레벨 설정 수정 (관리자)
     */
    @Transactional
    public boolean updateLevel(LevelSettingDTO levelDTO) {
        try {
            pointMapper.updateLevelSetting(levelDTO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 레벨 설정 삭제 (관리자)
     */
    @Transactional
    public boolean deleteLevel(int levelId) {
        try {
            pointMapper.deleteLevelSetting(levelId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
