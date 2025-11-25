package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 설명: 일일 퀴즈 퀘스트 엔티티
 * - 사용자별 일일 퀴즈 3개 저장
 */
@Entity
@Table(name = "DAILYQUEST",
        uniqueConstraints = @UniqueConstraint(columnNames = {"USERID", "QUESTDATE"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUESTID")
    private Long questId;

    @Column(name = "USERID", nullable = false)
    private Long userId;

    @Column(name = "QUESTDATE", nullable = false)
    private LocalDate questDate;

    @Column(name = "QUIZIDSJSON", columnDefinition = "CLOB")
    private String quizIdsJson; // JSON 문자열로 저장

    @Column(name = "COMPLETEDCOUNT", nullable = false)
    @Builder.Default
    private Integer completedCount = 0;

    @Column(name = "LASTCOMPLETEDTIME")
    private LocalDateTime lastCompletedTime; // 마지막 퀴즈 완료 시간 (작성자: 진원, 2025-11-24)

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