package com.planifai.core.finance.application;

public class SavingsGoalNotFoundException extends RuntimeException {

    public SavingsGoalNotFoundException(Long id) {
        super("Savings goal not found: " + id);
    }
}
