package com.planifai.core.finance.domain.model.cashflow;

import lombok.Builder;

import java.time.YearMonth;
import java.util.List;

@Builder
public record Cashflow(
        YearMonth from,
        YearMonth to,
        List<CashflowMonth> months
) {
}
