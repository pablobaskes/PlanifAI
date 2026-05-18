package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;

public record FinanceCategoryStatistic(
        ExpenseCategory category,
        BigDecimal amount,
        BigDecimal percentage
) {
}
