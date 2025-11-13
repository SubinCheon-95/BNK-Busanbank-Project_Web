package kr.co.busanbank.project.dto.quiz;

import lombok.*;
import java.util.List;

/**
 * 퀴즈 조회 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {
    private Long quizId;
    private String question;
    private List<String> options;
    private String explanation;
    private String category;
    private Integer difficulty;
    private Integer correctAnswer;
}

