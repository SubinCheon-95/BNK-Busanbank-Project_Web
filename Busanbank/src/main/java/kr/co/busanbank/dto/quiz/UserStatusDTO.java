package kr.co.busanbank.dto.quiz;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 사용자 상태 DTO
 * 작성자: 진원, 2025-11-24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusDTO {
    private Long userId;
    private Integer totalPoints;
    private Integer currentLevel;
    private String tier;
    private Integer completedQuizzes;
    private Integer correctRate;
    private Integer completedToday;
    private Boolean todayQuestCompleted; // 오늘 퀴즈 완료 여부
    private LocalDateTime lastCompletedTime; // 마지막 완료 시간 (작성자: 진원, 2025-11-24)
}