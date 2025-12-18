package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 수정일: 2025-12-17 (INTERESTBONUS 컬럼 제거 - 진원)
 * 설명: 사용자 레벨 엔티티
 * - 포인트 및 레벨 정보 저장
 */
@Entity
@Table(name = "USERLEVEL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEVELID")
    private Long levelId;

    @Column(name = "USERID", nullable = false, unique = true)
    private Long userId;

    @Column(name = "TOTALPOINTS", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "CURRENTLEVEL", nullable = false)
    @Builder.Default
    private Integer currentLevel = 1; // 1=Rookie, 2=Analyst, 3=Banker

    @Column(name = "TIER", length = 50)
    @Builder.Default
    private String tier = "Rookie"; // Rookie, Analyst, Banker

    @Column(name = "CREATEDDATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATEDDATE", nullable = false)
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

    /**
     * 포인트 업데이트 및 자동 레벨 계산
     */
    public void addPoints(Integer points) {
        this.totalPoints += points;
        updateLevel();
    }

    private void updateLevel() {
        if (this.totalPoints >= 500) {
            this.currentLevel = 3;
            this.tier = "Banker";
        } else if (this.totalPoints >= 200) {
            this.currentLevel = 2;
            this.tier = "Analyst";
        } else {
            this.currentLevel = 1;
            this.tier = "Rookie";
        }
    }
}