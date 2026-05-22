package com.planifai.core.finance.domain.model.recurring;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record MonthlyObligationsSummary(
        YearMonth month,
        BigDecimal totalRecurringObligations,
        BigDecimal pendingObligations,
        BigDecimal paidOrRegisteredObligations,
        BigDecimal realAvailableMoney,
        List<UpcomingPayment> upcomingPayments
) {
}
