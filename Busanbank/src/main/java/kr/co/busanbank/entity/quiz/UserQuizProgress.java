package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_QUIZ_PROGRESS",
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