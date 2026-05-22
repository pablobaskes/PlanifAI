package com.planifai.core.finance.domain.exception;

import com.planifai.core.finance.domain.FinanceConstants;

public class SavingsGoalNotFoundException extends RuntimeException {

    public SavingsGoalNotFoundException(Long id) {
        super(FinanceConstants.SAVINGS_GOAL_NOT_FOUND_PREFIX + id);
    }
}
