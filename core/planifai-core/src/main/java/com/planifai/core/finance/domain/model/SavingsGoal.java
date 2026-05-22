package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class SavingsGoal {

    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private SavingsGoalCategory category;
    private SavingsGoalStatus status;
    private BigDecimal monthlySavingRate;
    private String notes;
    private OffsetDateTime createdAt;

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Savings goal name is required.");
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Savings goal target amount must be greater than zero.");
        }
        if (currentAmount == null || currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Savings goal current amount cannot be negative.");
        }
        if (currentAmount.compareTo(targetAmount) > 0) {
            throw new IllegalArgumentException("Savings goal current amount cannot exceed target amount.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Savings goal category is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Savings goal status is required.");
        }
        if (monthlySavingRate != null && monthlySavingRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Savings goal monthly saving rate cannot be negative.");
        }
    }

    public boolean isCompleted() {
        return currentAmount != null
                && targetAmount != null
                && currentAmount.compareTo(targetAmount) >= 0;
    }

    public BigDecimal remainingAmount() {
        if (targetAmount == null || currentAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = targetAmount.subtract(currentAmount);
        return remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;
    }

    public BigDecimal progressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0 || currentAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal progressPercentage = currentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return progressPercentage.compareTo(BigDecimal.valueOf(100)) > 0
                ? BigDecimal.valueOf(100)
                : progressPercentage;
    }

    public Integer estimatedMonthsToCompletion() {
        BigDecimal remainingAmount = remainingAmount();
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        if (monthlySavingRate == null || monthlySavingRate.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return remainingAmount
                .divide(monthlySavingRate, 0, RoundingMode.CEILING)
                .intValue();
    }

    public LocalDate estimatedCompletionDate(LocalDate baselineDate) {
        Integer monthsToCompletion = estimatedMonthsToCompletion();
        if (monthsToCompletion == null || baselineDate == null) {
            return null;
        }
        return baselineDate.plusMonths(monthsToCompletion);
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
