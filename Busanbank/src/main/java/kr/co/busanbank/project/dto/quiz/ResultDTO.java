package kr.co.busanbank.project.dto.quiz;

import lombok.*; /**
 * 결과 조회 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultDTO {
    private Integer correctRate;
    private Integer earnedPoints;
    private Integer totalPoints;
    private Integer correctCount;
    private Integer incorrectCount;
    private String timeSpent;
    private Boolean leveledUp;
    private String newTier;
    private String levelUpMessage;
    private Boolean needMorePoints;
    private Integer pointsNeeded;
}
