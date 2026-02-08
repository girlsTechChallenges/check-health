package com.fiap.check.health.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.model.Frequency;
import com.fiap.check.health.model.Progress;
import com.fiap.check.health.model.Reward;

@Entity
@Table(name = "goals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalCategory category;

    @Column(nullable = false)
    private String type; // diária, semanal, mensal, pontual

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Embedded
    private Frequency frequency;

    private String difficulty; // fácil, média, difícil

    @Embedded
    private Reward reward;

    private String status; // ativa, concluída, arquivada

    private Boolean notifications;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Embedded
    private Progress progress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}