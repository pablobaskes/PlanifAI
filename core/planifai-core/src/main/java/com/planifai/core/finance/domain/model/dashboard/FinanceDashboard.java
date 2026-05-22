package com.planifai.core.finance.domain.model.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record FinanceDashboard(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        BigDecimal savingsAmount,
        BigDecimal savingsRate,
        FinanceHealthStatus healthStatus,
        List<ExpenseCategoryBreakdown> expensesByCategory
) {
}
