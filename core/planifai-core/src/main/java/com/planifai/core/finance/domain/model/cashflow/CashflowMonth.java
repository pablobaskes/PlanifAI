package com.planifai.core.finance.domain.model.cashflow;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;

@Builder
public record CashflowMonth(
        YearMonth month,
        BigDecimal expectedIncome,
        BigDecimal expectedExpenses,
        BigDecimal projectedBalance,
        BigDecimal netCashflow,
        BigDecimal savingsAmount,
        BigDecimal savingsRate
) {
}
