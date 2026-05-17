package com.planifai.core.finance.application;

public class RecurringExpenseNotFoundException extends RuntimeException {

    public RecurringExpenseNotFoundException(Long id) {
        super("Recurring expense not found: " + id);
    }
}
