package com.planifai.core.finance.domain.model.timeline;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record FinancialTimeline(
        LocalDate from,
        LocalDate to,
        List<FinancialTimelineEvent> events
) {
}
