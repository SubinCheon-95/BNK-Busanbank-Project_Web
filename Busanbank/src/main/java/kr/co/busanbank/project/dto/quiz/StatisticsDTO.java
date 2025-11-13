package kr.co.busanbank.project.dto.quiz;

import lombok.*; /**
 * 통계 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsDTO {
    private Integer totalQuizzes;
    private Integer todayAttempts;
    private Integer averageCorrectRate;
    private Integer activeUsers;
}
