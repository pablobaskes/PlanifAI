package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;

public record ExpenseCategoryBreakdown(
        ExpenseCategory category,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}
