package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

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
