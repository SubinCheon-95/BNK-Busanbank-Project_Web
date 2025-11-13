package kr.co.busanbank.project.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "daily_quest",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "quest_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate questDate;

    @Column(columnDefinition = "CLOB")
    private String quizIdsJson; // JSON 문자열로 저장

    @Column(nullable = false)
    @Builder.Default
    private Integer completedCount = 0;

    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 문자열을 List로 변환
     */
    public List<Long> getQuizIds() {
        if (quizIdsJson == null || quizIdsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(quizIdsJson, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * List를 JSON 문자열로 변환
     */
    public void setQuizIds(List<Long> quizIds) {
        try {
            this.quizIdsJson = objectMapper.writeValueAsString(quizIds);
        } catch (JsonProcessingException e) {
            this.quizIdsJson = "[]";
        }
    }

    public boolean isCompleted() {
        return completedCount >= 3;
    }

    public void incrementCompleted() {
        this.completedCount++;
    }
}