package kr.co.busanbank.project.repository.quiz;

import kr.co.busanbank.project.entity.quiz.UserQuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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
     * 오늘 사용자가 푼 퀴즈 개수 - Oracle 문법
     */
    @Query(value = "SELECT COUNT(*) FROM user_quiz_progress " +
            "WHERE user_id = ?1 AND TRUNC(submitted_at) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer countTodayQuizzes(Long userId);

    /**
     * 오늘 사용자가 얻은 총 포인트 - Oracle 문법
     */
    @Query(value = "SELECT NVL(SUM(earned_points), 0) FROM user_quiz_progress " +
            "WHERE user_id = ?1 AND TRUNC(submitted_at) = TRUNC(SYSDATE)",
            nativeQuery = true)
    Integer getTodayTotalPoints(Long userId);

    /**
     * 사용자의 정답 개수 - Oracle 문법
     */
    @Query(value = "SELECT COUNT(*) FROM user_quiz_progress " +
            "WHERE user_id = ?1 AND is_correct = 1",
            nativeQuery = true)
    Integer countCorrectAnswers(Long userId);

    /**
     * 사용자의 전체 풀이 개수
     */
    @Query(value = "SELECT COUNT(*) FROM user_quiz_progress WHERE user_id = ?1",
            nativeQuery = true)
    Integer countTotalAttempts(Long userId);

    /**
     * 사용자의 정답률 - Oracle 문법
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) = 0 THEN 0 " +
            "ELSE CAST(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS INTEGER) END " +
            "FROM user_quiz_progress WHERE user_id = ?1",
            nativeQuery = true)
    Integer getCorrectRate(Long userId);
}