package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "QUIZ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUIZID")
    private Long quizId;

    @Column(name = "QUESTION", nullable = false, length = 500)
    private String question;

    @Column(name = "OPTIONSJSON", columnDefinition = "CLOB", nullable = false)
    private String optionsJson; // JSON 문자열로 저장

    @Column(name = "CORRECTANSWER", nullable = false)
    private Integer correctAnswer;

    @Column(name = "EXPLANATION", length = 1000)
    private String explanation;

    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category; // FINANCE, INVESTMENT, SAVINGS, CREDIT, LOAN

    @Column(name = "DIFFICULTY", nullable = false)
    private Integer difficulty; // 1~5

    @Column(name = "CREATEDDATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATEDDATE")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Getter/Setter for options (List)
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 문자열을 List로 변환
     */
    public List<String> getOptions() {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, List.class);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * List를 JSON 문자열로 변환
     */
    public void setOptions(List<String> options) {
        try {
            this.optionsJson = objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            this.optionsJson = "[]";
        }
    }
}