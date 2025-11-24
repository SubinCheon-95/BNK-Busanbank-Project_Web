package kr.co.busanbank.repository.quiz;

import kr.co.busanbank.entity.quiz.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 카테고리별 퀴즈 조회
     */
    List<Quiz> findByCategory(String category);

    /**
     * 난이도별 퀴즈 조회
     */
    List<Quiz> findByDifficulty(Integer difficulty);

    /**
     * 랜덤으로 3개 퀴즈 조회 (데일리 퀴즈용) - Oracle 문법
     */
    @Query(value = "SELECT * FROM QUIZ ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 3 ROWS ONLY", nativeQuery = true)
    List<Quiz> findRandomQuizzes();

    /**
     * 특정 카테고리에서 랜덤 퀴즈 - Oracle 문법
     */
    @Query(value = "SELECT * FROM QUIZ WHERE CATEGORY = ?1 ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ?2 ROWS ONLY", nativeQuery = true)
    List<Quiz> findRandomQuizzesByCategory(String category, Integer limit);

    /**
     * 관리자용 페이징 조회 메서드들
     */
    Page<Quiz> findByCategory(String category, Pageable pageable);

    Page<Quiz> findByDifficulty(Integer difficulty, Pageable pageable);

    Page<Quiz> findByCategoryAndDifficulty(String category, Integer difficulty, Pageable pageable);

    Page<Quiz> findByQuestionContaining(String question, Pageable pageable);

    Page<Quiz> findByQuestionContainingAndCategory(String question, String category, Pageable pageable);

    Page<Quiz> findByQuestionContainingAndDifficulty(String question, Integer difficulty, Pageable pageable);

    Page<Quiz> findByQuestionContainingAndCategoryAndDifficulty(String question, String category, Integer difficulty, Pageable pageable);
}