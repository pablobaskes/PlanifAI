package com.planifai.core.finance.domain.model.budget;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record BudgetSummary(
        YearMonth month,
        BigDecimal totalLimitAmount,
        BigDecimal totalConsumedAmount,
        BigDecimal totalRemainingAmount,
        BigDecimal totalOverspentAmount,
        BigDecimal overallConsumptionPercentage,
        BudgetStatus status,
        List<BudgetCategoryStatus> categories,
        List<BudgetAlert> alerts
) {
}
