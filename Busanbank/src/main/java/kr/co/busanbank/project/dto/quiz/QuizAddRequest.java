package kr.co.busanbank.project.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; /**
 * 관리자 퀴즈 추가 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAddRequest {
    private String question;
    private List<String> options;
    private Integer correctAnswer;
    private String explanation;
    private String category;
    private Integer difficulty;
}
