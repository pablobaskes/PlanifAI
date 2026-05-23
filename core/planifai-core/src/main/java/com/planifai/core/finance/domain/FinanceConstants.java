package com.planifai.core.finance.domain;

import java.math.BigDecimal;

public final class FinanceConstants {

    public static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);
    public static final BigDecimal GOOD_SAVINGS_RATE_THRESHOLD = BigDecimal.valueOf(20);
    public static final BigDecimal BUDGET_WARNING_PERCENTAGE = BigDecimal.valueOf(80);
    public static final int MIN_PAYMENT_DAY = 1;
    public static final int MAX_PAYMENT_DAY = 31;

    public static final String DASHBOARD_MONTH_REQUIRED = "Dashboard month is required.";
    public static final String TIMELINE_RANGE_REQUIRED = "Timeline from and to dates are required.";
    public static final String TIMELINE_RANGE_INVALID = "Timeline from date cannot be after to date.";
    public static final String CASHFLOW_RANGE_REQUIRED = "Cashflow from and to months are required.";
    public static final String CASHFLOW_RANGE_INVALID = "Cashflow to month cannot be before from month.";
    public static final String CATEGORY_STATISTICS_MONTH_REQUIRED = "Category statistics month is required.";
    public static final String OBLIGATIONS_SUMMARY_MONTH_REQUIRED = "Obligations summary month is required.";
    public static final String INVALID_MONTH = "Invalid month.";
    public static final String RECURRING_EXPENSE_ID_REQUIRED = "Recurring expense id is required.";
    public static final String SAVINGS_GOAL_ID_REQUIRED = "Savings goal id is required.";
    public static final String BUDGET_ID_REQUIRED = "Budget id is required.";
    public static final String BUDGET_REQUIRED = "Budget is required.";
    public static final String BUDGET_MONTH_REQUIRED = "Budget month is required.";
    public static final String BUDGET_CATEGORY_REQUIRED = "Budget category is required.";
    public static final String BUDGET_LIMIT_AMOUNT_POSITIVE = "Budget limit amount must be greater than zero.";
    public static final String BUDGET_ACTIVE_DUPLICATE =
            "An active budget already exists for this category and month.";
    public static final String EXPENSE_REQUIRED = "Expense is required.";
    public static final String EXPENSE_CONCEPT_REQUIRED = "Expense concept is required.";
    public static final String EXPENSE_AMOUNT_POSITIVE = "Expense amount must be positive.";
    public static final String EXPENSE_DATE_REQUIRED = "Expense date is required.";
    public static final String EXPENSE_CATEGORY_REQUIRED = "Expense category is required.";
    public static final String INCOME_REQUIRED = "Income is required.";
    public static final String INCOME_SOURCE_REQUIRED = "Income source is required.";
    public static final String INCOME_AMOUNT_POSITIVE = "Income amount must be positive.";
    public static final String INCOME_DATE_REQUIRED = "Income date is required.";
    public static final String RECURRING_EXPENSE_REQUIRED = "Recurring expense is required.";
    public static final String RECURRING_EXPENSE_NAME_REQUIRED = "Recurring expense name is required.";
    public static final String RECURRING_EXPENSE_AMOUNT_POSITIVE = "Recurring expense amount must be positive.";
    public static final String RECURRING_EXPENSE_RECURRENCE_REQUIRED = "Recurring expense recurrence is required.";
    public static final String RECURRING_EXPENSE_CATEGORY_REQUIRED = "Recurring expense category is required.";
    public static final String RECURRING_EXPENSE_PAYMENT_DAY_RANGE =
            "Recurring expense payment day must be between 1 and 31.";
    public static final String RECURRING_EXPENSE_START_DATE_REQUIRED = "Recurring expense start date is required.";
    public static final String RECURRING_EXPENSE_END_DATE_BEFORE_START =
            "Recurring expense end date cannot be before start date.";
    public static final String SAVINGS_GOAL_REQUIRED = "Savings goal is required.";
    public static final String SAVINGS_GOAL_NAME_REQUIRED = "Savings goal name is required.";
    public static final String SAVINGS_GOAL_TARGET_AMOUNT_POSITIVE =
            "Savings goal target amount must be greater than zero.";
    public static final String SAVINGS_GOAL_CURRENT_AMOUNT_NEGATIVE =
            "Savings goal current amount cannot be negative.";
    public static final String SAVINGS_GOAL_CURRENT_AMOUNT_EXCEEDS_TARGET =
            "Savings goal current amount cannot exceed target amount.";
    public static final String SAVINGS_GOAL_CATEGORY_REQUIRED = "Savings goal category is required.";
    public static final String SAVINGS_GOAL_STATUS_REQUIRED = "Savings goal status is required.";
    public static final String SAVINGS_GOAL_MONTHLY_RATE_NEGATIVE =
            "Savings goal monthly saving rate cannot be negative.";
    public static final String RECURRING_EXPENSE_NOT_FOUND_PREFIX = "Recurring expense not found: ";
    public static final String SAVINGS_GOAL_NOT_FOUND_PREFIX = "Savings goal not found: ";
    public static final String BUDGET_NOT_FOUND_PREFIX = "Budget not found: ";
    public static final String RECURRING_EXPENSE_RECURRENCE_UNSUPPORTED =
            "Recurring expense recurrence must be MONTHLY or YEARLY.";

    private FinanceConstants() {
    }
}
