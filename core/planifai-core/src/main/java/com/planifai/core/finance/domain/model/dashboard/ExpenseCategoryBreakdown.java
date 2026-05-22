package com.planifai.core.finance.domain.model.dashboard;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ExpenseCategoryBreakdown(
        ExpenseCategory category,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}
