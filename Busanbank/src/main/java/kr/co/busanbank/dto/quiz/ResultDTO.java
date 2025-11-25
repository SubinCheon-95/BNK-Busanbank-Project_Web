package kr.co.busanbank.dto.quiz;

import lombok.*; /**
 * 결과 조회 DTO
 * 작성자: 진원, 2025-11-25
 * 수정: 오늘 통계와 누적 통계 분리
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultDTO {
    // 오늘의 통계
    private Integer todayCorrectCount;
    private Integer todayIncorrectCount;
    private Integer todayCorrectRate;
    private Integer earnedPoints;
    private String timeSpent;

    // 누적 통계
    private Integer totalPoints;
    private Integer correctCount;
    private Integer incorrectCount;
    private Integer correctRate;

    // 레벨 정보
    private Boolean leveledUp;
    private String newTier;
    private String levelUpMessage;
    private Boolean needMorePoints;
    private Integer pointsNeeded;
}