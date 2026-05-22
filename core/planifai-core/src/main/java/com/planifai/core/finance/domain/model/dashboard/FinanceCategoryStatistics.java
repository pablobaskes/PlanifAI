package com.planifai.core.finance.domain.model.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record FinanceCategoryStatistics(
        YearMonth month,
        BigDecimal totalExpenses,
        List<FinanceCategoryStatistic> categories
) {
}
