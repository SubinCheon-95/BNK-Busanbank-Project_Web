package kr.co.busanbank.repository.quiz;

import kr.co.busanbank.entity.quiz.UserQuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserQuizProgressRepository extends JpaRepository<UserQuizProgress, Long> {

    /**
     * 사용자가 특정 날짜에 푼 퀴즈 조회
     */
    List<UserQuizProgress> findByUserIdAndSubmittedAtBetween(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자가 특정 날짜에 푼 퀴즈 조회 (Oracle 날짜 기준)
     * 작성자: 진원, 2025-11-24
     */
    @Query(value = "SELECT * FROM USERQUIZPROGRESS " +
            "WHERE USERID = :userId " +
            "AND TRUNC(SUBMITTEDAT) = TO_DATE(:questDate, 'YYYY-MM-DD')",
            nativeQuery = true)
    List<UserQuizProgress> findByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("questDate") LocalDate questDate);

    /**
     * 오늘 사용자가 푼 퀴즈 개수 - Oracle 문법 (테이블명 수정: 진원, 2025-11-24)
     */
    @Query(value = "SELECT COUNT(*) FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer countTodayQuizzes(Long userId);

    /**
     * 오늘 사용자가 얻은 총 포인트 - Oracle 문법 (테이블명 수정: 진원, 2025-11-24)
     */
    @Query(value = "SELECT NVL(SUM(EARNEDPOINTS), 0) FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer getTodayTotalPoints(Long userId);

    /**
     * 사용자의 정답 개수 - Oracle 문법 (테이블명 수정: 진원, 2025-11-24)
     */
    @Query(value = "SELECT COUNT(*) FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND ISCORRECT = 1",
            nativeQuery = true)
    Integer countCorrectAnswers(Long userId);

    /**
     * 사용자의 전체 풀이 개수 (테이블명 수정: 진원, 2025-11-24)
     */
    @Query(value = "SELECT COUNT(*) FROM USERQUIZPROGRESS WHERE USERID = ?1",
            nativeQuery = true)
    Integer countTotalAttempts(Long userId);

    /**
     * 사용자의 정답률 - Oracle 문법 (테이블명 수정: 진원, 2025-11-24)
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) = 0 THEN 0 " +
            "ELSE CAST(SUM(CASE WHEN ISCORRECT = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS INTEGER) END " +
            "FROM USERQUIZPROGRESS WHERE USERID = ?1",
            nativeQuery = true)
    Integer getCorrectRate(Long userId);

    /**
     * 오늘 사용자의 정답 개수 - Oracle 문법 (작성자: 진원, 2025-11-25)
     */
    @Query(value = "SELECT COUNT(*) FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND ISCORRECT = 1 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer countTodayCorrectAnswers(Long userId);

    /**
     * 오늘 사용자의 오답 개수 - Oracle 문법 (작성자: 진원, 2025-11-25)
     */
    @Query(value = "SELECT COUNT(*) FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND ISCORRECT = 0 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer countTodayIncorrectAnswers(Long userId);

    /**
     * 오늘 사용자의 정답률 - Oracle 문법 (작성자: 진원, 2025-11-25)
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) = 0 THEN 0 " +
            "ELSE CAST(SUM(CASE WHEN ISCORRECT = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS INTEGER) END " +
            "FROM USERQUIZPROGRESS WHERE USERID = ?1 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer getTodayCorrectRate(Long userId);

    /**
     * 최근 퀴즈 세션(최대 3개)에서 획득한 포인트 합계 - Oracle 문법
     * 작성자: 진원, 2025-11-28
     * 설명: 오늘 푼 퀴즈 중 가장 최근 3개의 포인트만 합산
     */
    @Query(value = "SELECT NVL(SUM(EARNEDPOINTS), 0) FROM (" +
            "SELECT EARNEDPOINTS " +
            "FROM USERQUIZPROGRESS " +
            "WHERE USERID = ?1 AND TRUNC(SUBMITTEDAT) = TRUNC(SYSDATE) " +
            "ORDER BY SUBMITTEDAT DESC " +
            "FETCH FIRST 3 ROWS ONLY" +
            ")", nativeQuery = true)
    Integer getRecentSessionPoints(Long userId);
}