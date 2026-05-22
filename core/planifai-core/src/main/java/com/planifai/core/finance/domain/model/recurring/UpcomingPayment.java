package com.planifai.core.finance.domain.model.recurring;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record UpcomingPayment(
        Long recurringExpenseId,
        String name,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate dueDate,
        Integer paymentDay,
        ObligationPaymentStatus status
) {
}
