package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalsSummary(
        Integer totalGoals,
        Integer activeGoals,
        Integer completedGoals,
        Integer pausedGoals,
        Integer cancelledGoals,
        BigDecimal totalTargetAmount,
        BigDecimal totalCurrentAmount,
        BigDecimal totalRemainingAmount,
        BigDecimal overallProgressPercentage,
        BigDecimal monthlySavingRate,
        Integer estimatedMonthsToCompletion,
        LocalDate estimatedCompletionDate,
        SavingsGoal nearestGoalToComplete
) {
}
