package kr.co.busanbank.entity.quiz;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_LEVEL")
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

    @Column(name = "INTERESTBONUS", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal interestBonus = new BigDecimal("0.00"); // +0.1%, +0.2%

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
            this.interestBonus = new BigDecimal("0.20");
        } else if (this.totalPoints >= 200) {
            this.currentLevel = 2;
            this.tier = "Analyst";
            this.interestBonus = new BigDecimal("0.10");
        } else {
            this.currentLevel = 1;
            this.tier = "Rookie";
            this.interestBonus = new BigDecimal("0.00");
        }
    }
}