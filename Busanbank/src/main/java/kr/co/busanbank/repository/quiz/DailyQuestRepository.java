package kr.co.busanbank.repository.quiz;

import kr.co.busanbank.entity.quiz.DailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyQuestRepository extends JpaRepository<DailyQuest, Long> {

    /**
     * 사용자의 오늘 퀴즈 조회
     */
    Optional<DailyQuest> findByUserIdAndQuestDate(Long userId, LocalDate questDate);
}
