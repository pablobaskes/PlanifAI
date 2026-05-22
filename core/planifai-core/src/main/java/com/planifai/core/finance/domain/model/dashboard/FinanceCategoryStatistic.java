package com.planifai.core.finance.domain.model.dashboard;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FinanceCategoryStatistic(
        ExpenseCategory category,
        BigDecimal amount,
        BigDecimal percentage
) {
}
