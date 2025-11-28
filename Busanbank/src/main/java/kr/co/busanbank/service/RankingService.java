package kr.co.busanbank.service;

import kr.co.busanbank.dto.RankingDTO;
import kr.co.busanbank.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 랭킹 서비스
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private final PointMapper pointMapper;

    /**
     * 전체 랭킹 TOP 10 조회
     */
    public List<RankingDTO> getTopRankingAll(int limit) {
        return pointMapper.selectRankingAll(limit);
    }

    /**
     * 월별 랭킹 TOP 10 조회
     */
    public List<RankingDTO> getTopRankingMonthly(int year, int month, int limit) {
        return pointMapper.selectRankingMonthly(year, month, limit);
    }

    /**
     * 현재 월 랭킹 TOP 10 조회
     */
    public List<RankingDTO> getTopRankingCurrentMonth(int limit) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0부터 시작
        return pointMapper.selectRankingMonthly(year, month, limit);
    }

    /**
     * 사용자 전체 순위 조회
     */
    public Integer getUserRankAll(int userId) {
        return pointMapper.selectUserRankAll(userId);
    }

    /**
     * 사용자 월별 순위 조회
     */
    public Integer getUserRankMonthly(int userId, int year, int month) {
        return pointMapper.selectUserRankMonthly(userId, year, month);
    }

    /**
     * 사용자 현재 월 순위 조회
     */
    public Integer getUserRankCurrentMonth(int userId) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return pointMapper.selectUserRankMonthly(userId, year, month);
    }

    /**
     * 랭킹 페이지용 종합 데이터 조회
     */
    public Map<String, Object> getRankingData(int userId) {
        Map<String, Object> result = new HashMap<>();

        // 전체 TOP 10
        List<RankingDTO> allTimeTop10 = getTopRankingAll(10);
        result.put("allTimeTop10", allTimeTop10);

        // 이번 달 TOP 10
        List<RankingDTO> monthlyTop10 = getTopRankingCurrentMonth(10);
        result.put("monthlyTop10", monthlyTop10);

        // 내 전체 순위
        Integer myAllRank = getUserRankAll(userId);
        result.put("myAllRank", myAllRank);

        // 내 이번 달 순위
        Integer myMonthlyRank = getUserRankCurrentMonth(userId);
        result.put("myMonthlyRank", myMonthlyRank);

        // 현재 연월
        Calendar cal = Calendar.getInstance();
        result.put("currentYear", cal.get(Calendar.YEAR));
        result.put("currentMonth", cal.get(Calendar.MONTH) + 1);

        return result;
    }
}
