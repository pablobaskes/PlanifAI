package com.planifai.core.finance.infrastructure.output.jpa.entity;

import com.planifai.core.finance.domain.model.goal.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.goal.SavingsGoalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "finance_savings_goals")
@Getter
@Setter
@NoArgsConstructor
public class SavingsGoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SavingsGoalCategory category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SavingsGoalStatus status;
    @Column(precision = 12, scale = 2)
    private BigDecimal monthlySavingRate;
    @Column(length = 1000)
    private String notes;
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
