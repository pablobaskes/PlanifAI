package com.planifai.core.finance.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

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
