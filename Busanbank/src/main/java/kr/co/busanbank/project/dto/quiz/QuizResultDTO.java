package kr.co.busanbank.project.dto.quiz;

import lombok.*; /**
 * 퀴즈 제출 응답
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultDTO {
    private Boolean isCorrect;
    private Integer earnedPoints;
    private String explanation;
    private Integer newTotalPoints;
    private Integer totalEarnedToday;
    private Boolean leveledUp;
    private String newTier;
    private String levelUpMessage;
}
