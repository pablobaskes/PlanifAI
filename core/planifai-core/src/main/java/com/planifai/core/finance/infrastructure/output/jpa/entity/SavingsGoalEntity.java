package com.planifai.core.finance.infrastructure.output.jpa.entity;

import com.planifai.core.finance.domain.model.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "finance_savings_goals")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public SavingsGoalCategory getCategory() {
        return category;
    }

    public void setCategory(SavingsGoalCategory category) {
        this.category = category;
    }

    public SavingsGoalStatus getStatus() {
        return status;
    }

    public void setStatus(SavingsGoalStatus status) {
        this.status = status;
    }

    public BigDecimal getMonthlySavingRate() {
        return monthlySavingRate;
    }

    public void setMonthlySavingRate(BigDecimal monthlySavingRate) {
        this.monthlySavingRate = monthlySavingRate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
