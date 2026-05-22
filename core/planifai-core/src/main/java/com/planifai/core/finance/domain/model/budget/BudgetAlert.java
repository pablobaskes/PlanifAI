package com.planifai.core.finance.domain.model.budget;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BudgetAlert(
        BudgetAlertType type,
        ExpenseCategory category,
        BigDecimal limitAmount,
        BigDecimal consumedAmount,
        BigDecimal threshold,
        String message
) {
}
