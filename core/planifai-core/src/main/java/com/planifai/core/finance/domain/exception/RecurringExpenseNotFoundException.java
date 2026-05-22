package com.planifai.core.finance.domain.exception;

import com.planifai.core.finance.domain.FinanceConstants;

public class RecurringExpenseNotFoundException extends RuntimeException {

    public RecurringExpenseNotFoundException(Long id) {
        super(FinanceConstants.RECURRING_EXPENSE_NOT_FOUND_PREFIX + id);
    }
}
