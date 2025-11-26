package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 설명: 사용자 퀴즈 진행도 엔티티
 * - 사용자의 퀴즈 풀이 이력 저장
 * - 정답 여부 및 포인트 기록
 */
@Entity
@Table(name = "USERQUIZPROGRESS",
        indexes = {
                @Index(name = "idx_user_date", columnList = "USERID, SUBMITTEDAT"),
                @Index(name = "idx_quiz_id", columnList = "QUIZID")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROGRESSID")
    private Long progressId;

    @Column(name = "USERID", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUIZID", nullable = false)
    private Quiz quiz;

    @Column(name = "ISCORRECT", nullable = false)
    private Boolean isCorrect;

    @Column(name = "EARNEDPOINTS", nullable = false)
    private Integer earnedPoints;

    @Column(name = "SUBMITTEDAT", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}