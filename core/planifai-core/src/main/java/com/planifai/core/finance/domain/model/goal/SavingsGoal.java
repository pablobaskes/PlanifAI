package com.planifai.core.finance.domain.model.goal;

import com.planifai.core.finance.domain.FinanceConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_NAME_REQUIRED);
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_TARGET_AMOUNT_POSITIVE);
        }
        if (currentAmount == null || currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_CURRENT_AMOUNT_NEGATIVE);
        }
        if (currentAmount.compareTo(targetAmount) > 0) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_CURRENT_AMOUNT_EXCEEDS_TARGET);
        }
        if (category == null) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_CATEGORY_REQUIRED);
        }
        if (status == null) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_STATUS_REQUIRED);
        }
        if (monthlySavingRate != null && monthlySavingRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_MONTHLY_RATE_NEGATIVE);
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
                .multiply(FinanceConstants.MAX_PERCENTAGE)
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return progressPercentage.compareTo(FinanceConstants.MAX_PERCENTAGE) > 0
                ? FinanceConstants.MAX_PERCENTAGE
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

}
