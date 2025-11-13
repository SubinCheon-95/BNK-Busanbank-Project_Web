package kr.co.busanbank.project.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 퀴즈 제출 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {
    private Long userId;
    private Long quizId;
    private Integer selectedAnswer;
}
