package kr.co.busanbank.project.repository.quiz;


import kr.co.busanbank.project.entity.quiz.DailyQuest;
import kr.co.busanbank.project.entity.quiz.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    /**
     * userId로 사용자 레벨 조회
     */
    Optional<UserLevel> findByUserId(Long userId);
}

