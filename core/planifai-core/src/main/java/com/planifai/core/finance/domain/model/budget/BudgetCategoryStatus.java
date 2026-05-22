package com.planifai.core.finance.domain.model.budget;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record BudgetCategoryStatus(
        Long budgetId,
        ExpenseCategory category,
        BigDecimal limitAmount,
        BigDecimal consumedAmount,
        BigDecimal remainingAmount,
        BigDecimal overspentAmount,
        BigDecimal consumptionPercentage,
        BudgetStatus status,
        List<BudgetAlert> alerts
) {
}
