package com.planifai.core.finance.domain.exception;

import com.planifai.core.finance.domain.FinanceConstants;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException(Long id) {
        super(FinanceConstants.BUDGET_NOT_FOUND_PREFIX + id);
    }
}
