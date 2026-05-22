package com.planifai.core.finance.domain.model.budget;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BudgetAlert(
        BudgetAlertType type,
        BudgetStatus status,
        ExpenseCategory category,
        BigDecimal thresholdPercentage,
        String message
) {
}
