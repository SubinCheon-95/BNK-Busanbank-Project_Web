package kr.co.busanbank.dto.quiz;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 퀴즈 조회 DTO
 * 수정자: 진원
 * 수정일: 2025-11-24
 * 내용: createdDate, updatedDate 필드 추가
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
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}