package com.planifai.core.finance.domain.model.timeline;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FinancialTimelineEvent(
        String id,
        LocalDate date,
        FinancialTimelineEventType type,
        String label,
        BigDecimal amount,
        ExpenseCategory category,
        String source,
        boolean projected,
        FinancialTimelineEventStatus status
) {
}
