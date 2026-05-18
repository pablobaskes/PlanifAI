package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record FinanceCategoryStatistics(
        YearMonth month,
        BigDecimal totalExpenses,
        List<FinanceCategoryStatistic> categories
) {
}
