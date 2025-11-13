package kr.co.busanbank.project.dto.quiz;

import lombok.*; /**
 * 사용자 상태 DTO
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
}
