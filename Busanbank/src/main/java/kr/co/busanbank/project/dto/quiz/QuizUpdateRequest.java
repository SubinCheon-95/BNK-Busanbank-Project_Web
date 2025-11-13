package kr.co.busanbank.project.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 관리자 퀴즈 수정 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizUpdateRequest {
    private Long quizId;
    private String question;
    private String explanation;
    private String category;
    private Integer difficulty;
}
