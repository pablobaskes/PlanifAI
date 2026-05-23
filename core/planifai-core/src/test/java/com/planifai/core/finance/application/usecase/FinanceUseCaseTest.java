package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.ports.output.BudgetOutputPort;
import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.SavingsGoalOutputPort;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.budget.BudgetAlertType;
import com.planifai.core.finance.domain.model.budget.BudgetStatus;
import com.planifai.core.finance.domain.model.budget.BudgetSummary;
import com.planifai.core.finance.domain.model.cashflow.Cashflow;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.dashboard.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.dashboard.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.domain.model.recurring.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.recurring.ObligationPaymentStatus;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.recurring.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.domain.model.goal.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.goal.SavingsGoalStatus;
import com.planifai.core.finance.domain.model.goal.SavingsGoalsSummary;
import com.planifai.core.finance.domain.model.timeline.FinancialTimeline;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventStatus;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private final FakeExpenseOutputPort expenseOutputPort = new FakeExpenseOutputPort();
    private final FakeIncomeOutputPort incomeOutputPort = new FakeIncomeOutputPort();
    private final FakeRecurringExpenseOutputPort recurringExpenseOutputPort = new FakeRecurringExpenseOutputPort();
    private final FakeSavingsGoalOutputPort savingsGoalOutputPort = new FakeSavingsGoalOutputPort();
    private final FakeBudgetOutputPort budgetOutputPort = new FakeBudgetOutputPort();
    private final FinanceUseCase financeUseCase = new FinanceUseCase(
            expenseOutputPort,
            incomeOutputPort,
            recurringExpenseOutputPort,
            savingsGoalOutputPort,
            budgetOutputPort
    );

    @Test
    void getDashboardCalculatesMonthlySummarySavingsGoodHealthAndBreakdown() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 5)));
        incomeOutputPort.incomes.add(income("999.00", LocalDate.of(2026, 6, 1)));
        expenseOutputPort.expenses.add(expense("1000.00", LocalDate.of(2026, 5, 10), ExpenseCategory.HOUSING));
        expenseOutputPort.expenses.add(expense("500.00", LocalDate.of(2026, 5, 12), ExpenseCategory.FOOD));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 4, 30), ExpenseCategory.ENTERTAINMENT));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("3000.00", dashboard.totalIncome());
        assertBigDecimal("1500.00", dashboard.totalExpenses());
        assertBigDecimal("1500.00", dashboard.netBalance());
        assertBigDecimal("1500.00", dashboard.savingsAmount());
        assertBigDecimal("50.00", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.GOOD, dashboard.healthStatus());
        assertEquals(2, dashboard.expensesByCategory().size());
        assertBreakdown(dashboard.expensesByCategory().get(0), ExpenseCategory.HOUSING, "1000.00", "66.67");
        assertBreakdown(dashboard.expensesByCategory().get(1), ExpenseCategory.FOOD, "500.00", "33.33");
    }

    @Test
    void getDashboardReturnsZeroValuesForEmptyMonth() {
        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("0", dashboard.totalIncome());
        assertBigDecimal("0", dashboard.totalExpenses());
        assertBigDecimal("0", dashboard.netBalance());
        assertBigDecimal("0", dashboard.savingsAmount());
        assertBigDecimal("0", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.NO_DATA, dashboard.healthStatus());
        assertTrue(dashboard.expensesByCategory().isEmpty());
    }

    @Test
    void getDashboardHandlesZeroIncomeWithoutDivisionByZero() {
        expenseOutputPort.expenses.add(expense("100.00", LocalDate.of(2026, 5, 10), ExpenseCategory.UTILITIES));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("0", dashboard.totalIncome());
        assertBigDecimal("100.00", dashboard.totalExpenses());
        assertBigDecimal("-100.00", dashboard.netBalance());
        assertBigDecimal("-100.00", dashboard.savingsAmount());
        assertBigDecimal("0", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.BAD, dashboard.healthStatus());
    }

    @Test
    void getDashboardMarksPositiveBalanceBelowTwentyPercentAsWarning() {
        incomeOutputPort.incomes.add(income("1000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("900.00", LocalDate.of(2026, 5, 2), ExpenseCategory.OTHER));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("100.00", dashboard.netBalance());
        assertBigDecimal("10.00", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.WARNING, dashboard.healthStatus());
    }

    @Test
    void getDashboardRepresentsNegativeBalanceAsBad() {
        incomeOutputPort.incomes.add(income("1000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("1200.00", LocalDate.of(2026, 5, 2), ExpenseCategory.OTHER));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("-200.00", dashboard.netBalance());
        assertBigDecimal("-200.00", dashboard.savingsAmount());
        assertBigDecimal("-20.00", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.BAD, dashboard.healthStatus());
    }

    @Test
    void getDashboardGroupsExpensesByCategoryAndUsesOtherForMissingCategory() {
        incomeOutputPort.incomes.add(income("1000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("100.00", LocalDate.of(2026, 5, 2), ExpenseCategory.FOOD));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 5, 3), ExpenseCategory.FOOD));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 5, 4), null));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertEquals(2, dashboard.expensesByCategory().size());
        assertBreakdown(dashboard.expensesByCategory().get(0), ExpenseCategory.FOOD, "150.00", "75.00");
        assertBreakdown(dashboard.expensesByCategory().get(1), ExpenseCategory.OTHER, "50.00", "25.00");
    }

    @Test
    void createRecurringExpenseStoresValidExpenseWithDefaults() {
        RecurringExpense request = recurringExpense(
                null,
                "Internet",
                "49.99",
                ExpenseCategory.UTILITIES,
                RecurringExpenseRecurrence.MONTHLY,
                5,
                LocalDate.of(2026, 1, 1),
                null,
                null
        );

        RecurringExpense created = financeUseCase.createRecurringExpense(request);

        assertEquals(1L, created.getId());
        assertEquals("Internet", created.getName());
        assertBigDecimal("49.99", created.getAmount());
        assertEquals(Boolean.TRUE, created.getActive());
        assertEquals(ExpenseCategory.UTILITIES, created.getCategory());
        assertEquals(RecurringExpenseRecurrence.MONTHLY, created.getRecurrence());
    }

    @Test
    void createRecurringExpenseRejectsNonPositiveAmount() {
        RecurringExpense request = recurringExpense(
                null,
                "Invalid",
                "0.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                null,
                true
        );

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createRecurringExpense(request));
    }

    @Test
    void createRecurringExpenseRejectsInvalidPaymentDay() {
        RecurringExpense request = recurringExpense(
                null,
                "Invalid day",
                "100.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                32,
                LocalDate.of(2026, 1, 1),
                null,
                true
        );

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createRecurringExpense(request));
    }

    @Test
    void createExpenseRejectsMissingCategory() {
        Expense expense = expense("Electricity", "100.00", LocalDate.of(2026, 5, 2), null);

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createExpense(expense));
    }

    @Test
    void getExpensesFiltersByCategory() {
        expenseOutputPort.expenses.add(expense("Lunch", "25.00", LocalDate.of(2026, 5, 2), ExpenseCategory.RESTAURANTS));
        expenseOutputPort.expenses.add(expense("Internet", "50.00", LocalDate.of(2026, 5, 3), ExpenseCategory.UTILITIES));

        List<Expense> filteredExpenses = financeUseCase.getExpenses(ExpenseCategory.RESTAURANTS);

        assertEquals(1, filteredExpenses.size());
        assertEquals("Lunch", filteredExpenses.get(0).getConcept());
    }

    @Test
    void getRecurringExpensesFiltersByCategory() {
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Streaming",
                "12.00",
                ExpenseCategory.SUBSCRIPTIONS,
                RecurringExpenseRecurrence.MONTHLY,
                3,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                2L,
                "Rent",
                "900.00",
                ExpenseCategory.HOUSING,
                RecurringExpenseRecurrence.MONTHLY,
                1,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        List<RecurringExpense> filteredExpenses = financeUseCase.getRecurringExpenses(ExpenseCategory.SUBSCRIPTIONS);

        assertEquals(1, filteredExpenses.size());
        assertEquals("Streaming", filteredExpenses.get(0).getName());
    }

    @Test
    void getCategoryStatisticsCalculatesAmountsAndPercentages() {
        expenseOutputPort.expenses.add(expense("Lunch", "25.00", LocalDate.of(2026, 5, 2), ExpenseCategory.RESTAURANTS));
        expenseOutputPort.expenses.add(expense("Groceries", "75.00", LocalDate.of(2026, 5, 3), ExpenseCategory.FOOD));
        expenseOutputPort.expenses.add(expense("Other month", "100.00", LocalDate.of(2026, 6, 3), ExpenseCategory.FOOD));

        var statistics = financeUseCase.getCategoryStatistics(YearMonth.of(2026, 5));

        assertBigDecimal("100.00", statistics.totalExpenses());
        assertEquals(2, statistics.categories().size());
        assertEquals(ExpenseCategory.FOOD, statistics.categories().get(0).category());
        assertBigDecimal("75.00", statistics.categories().get(0).amount());
        assertBigDecimal("75.00", statistics.categories().get(0).percentage());
        assertEquals(ExpenseCategory.RESTAURANTS, statistics.categories().get(1).category());
        assertBigDecimal("25.00", statistics.categories().get(1).amount());
        assertBigDecimal("25.00", statistics.categories().get(1).percentage());
    }

    @Test
    void getCategoryStatisticsReturnsSafeValuesForEmptyMonth() {
        var statistics = financeUseCase.getCategoryStatistics(YearMonth.of(2026, 5));

        assertBigDecimal("0", statistics.totalExpenses());
        assertTrue(statistics.categories().isEmpty());
    }

    @Test
    void getMonthlyObligationsSummaryCalculatesTotalsPendingAndRealAvailableMoney() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("Utilities", "100.00", LocalDate.of(2026, 5, 5), ExpenseCategory.UTILITIES));
        expenseOutputPort.expenses.add(expense("Groceries", "500.00", LocalDate.of(2026, 5, 8), ExpenseCategory.FOOD));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Rent",
                "1000.00",
                ExpenseCategory.HOUSING,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                2L,
                "Utilities",
                "100.00",
                ExpenseCategory.UTILITIES,
                RecurringExpenseRecurrence.MONTHLY,
                5,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        MonthlyObligationsSummary summary = financeUseCase.getMonthlyObligationsSummary(YearMonth.of(2026, 5));

        assertBigDecimal("1100.00", summary.totalRecurringObligations());
        assertBigDecimal("1000.00", summary.pendingObligations());
        assertBigDecimal("100.00", summary.paidOrRegisteredObligations());
        assertBigDecimal("1400.00", summary.realAvailableMoney());
        assertEquals(2, summary.upcomingPayments().size());
        assertEquals(ObligationPaymentStatus.PAID_OR_REGISTERED, summary.upcomingPayments().get(0).status());
        assertEquals(ObligationPaymentStatus.PENDING, summary.upcomingPayments().get(1).status());
    }

    @Test
    void getMonthlyObligationsSummaryExcludesInactiveAndOutOfRangeObligations() {
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Active",
                "100.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                2L,
                "Inactive",
                "200.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                null,
                false
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                3L,
                "Future",
                "300.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 6, 1),
                null,
                true
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                4L,
                "Expired",
                "400.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 30),
                true
        ));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                5L,
                "Yearly other month",
                "500.00",
                ExpenseCategory.OTHER,
                RecurringExpenseRecurrence.YEARLY,
                10,
                LocalDate.of(2026, 6, 1),
                null,
                true
        ));

        MonthlyObligationsSummary summary = financeUseCase.getMonthlyObligationsSummary(YearMonth.of(2026, 5));

        assertBigDecimal("100.00", summary.totalRecurringObligations());
        assertBigDecimal("100.00", summary.pendingObligations());
        assertEquals(1, summary.upcomingPayments().size());
        assertEquals("Active", summary.upcomingPayments().get(0).name());
    }

    @Test
    void getMonthlyObligationsSummaryReturnsSafeValuesForMonthWithoutData() {
        MonthlyObligationsSummary summary = financeUseCase.getMonthlyObligationsSummary(YearMonth.of(2026, 5));

        assertBigDecimal("0", summary.totalRecurringObligations());
        assertBigDecimal("0", summary.pendingObligations());
        assertBigDecimal("0", summary.paidOrRegisteredObligations());
        assertBigDecimal("0", summary.realAvailableMoney());
        assertTrue(summary.upcomingPayments().isEmpty());
    }

    @Test
    void createSavingsGoalStoresValidGoal() {
        SavingsGoal request = savingsGoal(
                null,
                "Emergency fund",
                "5000.00",
                "1000.00",
                SavingsGoalCategory.EMERGENCY_FUND,
                SavingsGoalStatus.ACTIVE,
                null
        );

        SavingsGoal created = financeUseCase.createSavingsGoal(request);

        assertEquals(1L, created.getId());
        assertEquals("Emergency fund", created.getName());
        assertEquals(SavingsGoalCategory.EMERGENCY_FUND, created.getCategory());
        assertEquals(SavingsGoalStatus.ACTIVE, created.getStatus());
        assertBigDecimal("5000.00", created.getTargetAmount());
        assertBigDecimal("1000.00", created.getCurrentAmount());
    }

    @Test
    void createSavingsGoalRejectsNonPositiveTargetAmount() {
        SavingsGoal request = savingsGoal(
                null,
                "Invalid",
                "0.00",
                "0.00",
                SavingsGoalCategory.OTHER,
                SavingsGoalStatus.ACTIVE,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createSavingsGoal(request));
    }

    @Test
    void createSavingsGoalRejectsNegativeCurrentAmount() {
        SavingsGoal request = savingsGoal(
                null,
                "Invalid",
                "1000.00",
                "-1.00",
                SavingsGoalCategory.OTHER,
                SavingsGoalStatus.ACTIVE,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createSavingsGoal(request));
    }

    @Test
    void getSavingsGoalsListsPersistedGoals() {
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "250.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                null
        ));
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                2L,
                "Laptop",
                "1200.00",
                "1200.00",
                SavingsGoalCategory.ELECTRONICS,
                SavingsGoalStatus.COMPLETED,
                null
        ));

        List<SavingsGoal> savingsGoals = financeUseCase.getSavingsGoals();

        assertEquals(2, savingsGoals.size());
        assertEquals("Travel", savingsGoals.get(0).getName());
        assertEquals("Laptop", savingsGoals.get(1).getName());
    }

    @Test
    void updateSavingsGoalUpdatesExistingGoalAndPreservesId() {
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "250.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                null
        ));
        SavingsGoal update = savingsGoal(
                null,
                "Japan travel",
                "3000.00",
                "500.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.PAUSED,
                null
        );

        SavingsGoal updated = financeUseCase.updateSavingsGoal(1L, update);

        assertEquals(1L, updated.getId());
        assertEquals("Japan travel", updated.getName());
        assertEquals(SavingsGoalStatus.PAUSED, updated.getStatus());
        assertBigDecimal("3000.00", updated.getTargetAmount());
        assertBigDecimal("500.00", updated.getCurrentAmount());
    }

    @Test
    void deleteSavingsGoalRemovesExistingGoal() {
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Car",
                "8000.00",
                "1000.00",
                SavingsGoalCategory.CAR,
                SavingsGoalStatus.ACTIVE,
                null
        ));

        financeUseCase.deleteSavingsGoal(1L);

        assertTrue(savingsGoalOutputPort.savingsGoals.isEmpty());
    }

    @Test
    void savingsGoalCalculatesProgressAndRemainingAmount() {
        SavingsGoal savingsGoal = savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "500.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                null
        );

        assertBigDecimal("25.00", savingsGoal.progressPercentage());
        assertBigDecimal("1500.00", savingsGoal.remainingAmount());
    }

    @Test
    void getSavingsGoalCalculatesEtaWithPositiveCurrentMonthSavings() {
        YearMonth currentMonth = YearMonth.now();
        incomeOutputPort.incomes.add(income("1000.00", currentMonth.atDay(1)));
        expenseOutputPort.expenses.add(expense("400.00", currentMonth.atDay(2), ExpenseCategory.FOOD));
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "800.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                null
        ));

        SavingsGoal savingsGoal = financeUseCase.getSavingsGoalById(1L);

        assertBigDecimal("600.00", savingsGoal.getMonthlySavingRate());
        assertEquals(2, savingsGoal.estimatedMonthsToCompletion());
    }

    @Test
    void getSavingsGoalDoesNotBreakEtaWithZeroOrNegativeCurrentMonthSavings() {
        YearMonth currentMonth = YearMonth.now();
        incomeOutputPort.incomes.add(income("1000.00", currentMonth.atDay(1)));
        expenseOutputPort.expenses.add(expense("1200.00", currentMonth.atDay(2), ExpenseCategory.FOOD));
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "800.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                null
        ));

        SavingsGoal savingsGoal = financeUseCase.getSavingsGoalById(1L);

        assertBigDecimal("0", savingsGoal.getMonthlySavingRate());
        assertEquals(null, savingsGoal.estimatedMonthsToCompletion());
    }

    @Test
    void getSavingsGoalsSummaryReturnsSafeValuesWithoutGoals() {
        SavingsGoalsSummary summary = financeUseCase.getSavingsGoalsSummary();

        assertEquals(0, summary.totalGoals());
        assertEquals(0, summary.activeGoals());
        assertEquals(0, summary.completedGoals());
        assertBigDecimal("0", summary.totalTargetAmount());
        assertBigDecimal("0", summary.totalCurrentAmount());
        assertBigDecimal("0", summary.totalRemainingAmount());
        assertBigDecimal("0", summary.overallProgressPercentage());
        assertEquals(null, summary.nearestGoalToComplete());
    }

    @Test
    void getSavingsGoalsSummaryAggregatesActiveAndCompletedGoals() {
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                1L,
                "Travel",
                "2000.00",
                "500.00",
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE,
                new BigDecimal("250.00")
        ));
        savingsGoalOutputPort.savingsGoals.add(savingsGoal(
                2L,
                "Laptop",
                "1000.00",
                "1000.00",
                SavingsGoalCategory.ELECTRONICS,
                SavingsGoalStatus.COMPLETED,
                null
        ));

        SavingsGoalsSummary summary = financeUseCase.getSavingsGoalsSummary();

        assertEquals(2, summary.totalGoals());
        assertEquals(1, summary.activeGoals());
        assertEquals(1, summary.completedGoals());
        assertBigDecimal("3000.00", summary.totalTargetAmount());
        assertBigDecimal("1500.00", summary.totalCurrentAmount());
        assertBigDecimal("1500.00", summary.totalRemainingAmount());
        assertBigDecimal("50.00", summary.overallProgressPercentage());
        assertEquals("Travel", summary.nearestGoalToComplete().getName());
    }

    @Test
    void createBudgetStoresValidBudgetWithDefaultActive() {
        Budget request = budget(null, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "400.00", null);

        Budget created = financeUseCase.createBudget(request);

        assertEquals(1L, created.getId());
        assertEquals(YearMonth.of(2026, 5), created.getMonth());
        assertEquals(ExpenseCategory.FOOD, created.getCategory());
        assertBigDecimal("400.00", created.getLimitAmount());
        assertEquals(Boolean.TRUE, created.getActive());
    }

    @Test
    void createBudgetRejectsNonPositiveLimitAmount() {
        Budget request = budget(null, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "0.00", true);

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createBudget(request));
    }

    @Test
    void createBudgetRejectsDuplicateActiveBudgetForSameCategoryAndMonth() {
        budgetOutputPort.budgets.add(budget(1L, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "400.00", true));
        Budget duplicate = budget(null, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "500.00", true);

        assertThrows(IllegalArgumentException.class, () -> financeUseCase.createBudget(duplicate));
    }

    @Test
    void getBudgetsListsBudgetsByMonth() {
        budgetOutputPort.budgets.add(budget(1L, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "400.00", true));
        budgetOutputPort.budgets.add(budget(2L, YearMonth.of(2026, 6), ExpenseCategory.FOOD, "600.00", true));

        List<Budget> budgets = financeUseCase.getBudgets(YearMonth.of(2026, 5));

        assertEquals(1, budgets.size());
        assertEquals(YearMonth.of(2026, 5), budgets.get(0).getMonth());
    }

    @Test
    void updateBudgetUpdatesExistingBudgetAndPreservesId() {
        budgetOutputPort.budgets.add(budget(1L, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "400.00", true));
        Budget update = budget(null, YearMonth.of(2026, 5), ExpenseCategory.RESTAURANTS, "250.00", false);

        Budget updated = financeUseCase.updateBudget(1L, update);

        assertEquals(1L, updated.getId());
        assertEquals(ExpenseCategory.RESTAURANTS, updated.getCategory());
        assertBigDecimal("250.00", updated.getLimitAmount());
        assertEquals(Boolean.FALSE, updated.getActive());
    }

    @Test
    void deleteBudgetRemovesExistingBudget() {
        budgetOutputPort.budgets.add(budget(1L, YearMonth.of(2026, 5), ExpenseCategory.FOOD, "400.00", true));

        financeUseCase.deleteBudget(1L);

        assertTrue(budgetOutputPort.budgets.isEmpty());
    }

    @Test
    void getBudgetSummaryCalculatesConsumptionRemainingOverspentStatusesAndAlerts() {
        YearMonth month = YearMonth.of(2026, 5);
        budgetOutputPort.budgets.add(budget(1L, month, ExpenseCategory.FOOD, "100.00", true));
        budgetOutputPort.budgets.add(budget(2L, month, ExpenseCategory.TRANSPORT, "100.00", true));
        budgetOutputPort.budgets.add(budget(3L, month, ExpenseCategory.ENTERTAINMENT, "100.00", true));
        budgetOutputPort.budgets.add(budget(4L, YearMonth.of(2026, 6), ExpenseCategory.FOOD, "999.00", true));
        budgetOutputPort.budgets.add(budget(5L, month, ExpenseCategory.HEALTH, "100.00", false));
        expenseOutputPort.expenses.add(expense("Groceries", "50.00", LocalDate.of(2026, 5, 2), ExpenseCategory.FOOD));
        expenseOutputPort.expenses.add(expense("Bus", "80.00", LocalDate.of(2026, 5, 3), ExpenseCategory.TRANSPORT));
        expenseOutputPort.expenses.add(expense("Movies", "125.00", LocalDate.of(2026, 5, 4), ExpenseCategory.ENTERTAINMENT));
        expenseOutputPort.expenses.add(expense("Other month", "999.00", LocalDate.of(2026, 6, 1), ExpenseCategory.FOOD));

        BudgetSummary summary = financeUseCase.getBudgetSummary(month);

        assertBigDecimal("300.00", summary.totalLimitAmount());
        assertBigDecimal("255.00", summary.totalConsumedAmount());
        assertBigDecimal("70.00", summary.totalRemainingAmount());
        assertBigDecimal("25.00", summary.totalOverspentAmount());
        assertBigDecimal("85.00", summary.overallConsumptionPercentage());
        assertEquals(BudgetStatus.WARNING, summary.status());
        assertEquals(3, summary.categories().size());

        assertEquals(ExpenseCategory.FOOD, summary.categories().get(0).category());
        assertBigDecimal("50.00", summary.categories().get(0).consumedAmount());
        assertBigDecimal("50.00", summary.categories().get(0).remainingAmount());
        assertBigDecimal("0", summary.categories().get(0).overspentAmount());
        assertEquals(BudgetStatus.OK, summary.categories().get(0).status());
        assertTrue(summary.categories().get(0).alerts().isEmpty());

        assertEquals(ExpenseCategory.TRANSPORT, summary.categories().get(1).category());
        assertBigDecimal("80.00", summary.categories().get(1).consumedAmount());
        assertEquals(BudgetStatus.WARNING, summary.categories().get(1).status());
        assertEquals(BudgetAlertType.APPROACHING_LIMIT, summary.categories().get(1).alerts().get(0).type());
        assertBigDecimal("100.00", summary.categories().get(1).alerts().get(0).limitAmount());
        assertBigDecimal("80.00", summary.categories().get(1).alerts().get(0).consumedAmount());
        assertBigDecimal("80", summary.categories().get(1).alerts().get(0).threshold());

        assertEquals(ExpenseCategory.ENTERTAINMENT, summary.categories().get(2).category());
        assertBigDecimal("125.00", summary.categories().get(2).consumedAmount());
        assertBigDecimal("0", summary.categories().get(2).remainingAmount());
        assertBigDecimal("25.00", summary.categories().get(2).overspentAmount());
        assertBigDecimal("125.00", summary.categories().get(2).consumptionPercentage());
        assertEquals(BudgetStatus.EXCEEDED, summary.categories().get(2).status());
        assertEquals(BudgetAlertType.BUDGET_EXCEEDED, summary.categories().get(2).alerts().get(0).type());
        assertEquals(2, summary.alerts().size());
    }

    @Test
    void getBudgetSummaryReturnsSafeValuesForMonthWithoutBudgets() {
        expenseOutputPort.expenses.add(expense("Groceries", "50.00", LocalDate.of(2026, 5, 2), ExpenseCategory.FOOD));

        BudgetSummary summary = financeUseCase.getBudgetSummary(YearMonth.of(2026, 5));

        assertBigDecimal("0", summary.totalLimitAmount());
        assertBigDecimal("0", summary.totalConsumedAmount());
        assertBigDecimal("0", summary.totalRemainingAmount());
        assertBigDecimal("0", summary.totalOverspentAmount());
        assertBigDecimal("0", summary.overallConsumptionPercentage());
        assertEquals(BudgetStatus.OK, summary.status());
        assertTrue(summary.categories().isEmpty());
        assertTrue(summary.alerts().isEmpty());
    }

    @Test
    void getBudgetSummaryReturnsZeroConsumptionForBudgetWithoutExpenses() {
        YearMonth month = YearMonth.of(2026, 5);
        budgetOutputPort.budgets.add(budget(1L, month, ExpenseCategory.FOOD, "100.00", true));

        BudgetSummary summary = financeUseCase.getBudgetSummary(month);

        assertEquals(1, summary.categories().size());
        assertBigDecimal("0", summary.categories().get(0).consumedAmount());
        assertBigDecimal("100.00", summary.categories().get(0).remainingAmount());
        assertBigDecimal("0", summary.categories().get(0).overspentAmount());
        assertEquals(BudgetStatus.OK, summary.categories().get(0).status());
        assertTrue(summary.categories().get(0).alerts().isEmpty());
    }

    @Test
    void getFinancialTimelineIncludesRealIncomesRealExpensesAndProjectedRecurringExpenses() {
        incomeOutputPort.incomes.add(income(1L, "Salary", "3000.00", LocalDate.of(2026, 5, 10)));
        expenseOutputPort.expenses.add(expense(2L, "Groceries", "150.00", LocalDate.of(2026, 5, 5), ExpenseCategory.FOOD));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                3L,
                "Rent",
                "900.00",
                ExpenseCategory.HOUSING,
                RecurringExpenseRecurrence.MONTHLY,
                1,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        FinancialTimeline timeline = financeUseCase.getFinancialTimeline(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        );

        assertEquals(3, timeline.events().size());
        assertEquals(FinancialTimelineEventType.RECURRING_EXPENSE, timeline.events().get(0).type());
        assertEquals(LocalDate.of(2026, 5, 1), timeline.events().get(0).date());
        assertEquals(true, timeline.events().get(0).projected());
        assertEquals(FinancialTimelineEventStatus.PROJECTED, timeline.events().get(0).status());
        assertBigDecimal("-900.00", timeline.events().get(0).amount());
        assertEquals(FinancialTimelineEventType.EXPENSE, timeline.events().get(1).type());
        assertEquals(false, timeline.events().get(1).projected());
        assertEquals(FinancialTimelineEventStatus.POSTED, timeline.events().get(1).status());
        assertBigDecimal("-150.00", timeline.events().get(1).amount());
        assertEquals(FinancialTimelineEventType.INCOME, timeline.events().get(2).type());
        assertEquals(false, timeline.events().get(2).projected());
        assertEquals(FinancialTimelineEventStatus.POSTED, timeline.events().get(2).status());
        assertBigDecimal("3000.00", timeline.events().get(2).amount());
    }

    @Test
    void getFinancialTimelineProjectsMonthlyRecurringExpensesInsideRange() {
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Subscription",
                "20.00",
                ExpenseCategory.SUBSCRIPTIONS,
                RecurringExpenseRecurrence.MONTHLY,
                31,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        FinancialTimeline timeline = financeUseCase.getFinancialTimeline(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertEquals(2, timeline.events().size());
        assertEquals(LocalDate.of(2026, 2, 28), timeline.events().get(0).date());
        assertEquals(LocalDate.of(2026, 3, 31), timeline.events().get(1).date());
        assertTrue(timeline.events().stream().allMatch(event -> event.projected()));
    }

    @Test
    void getFinancialTimelineReturnsEmptyEventsForEmptyRange() {
        FinancialTimeline timeline = financeUseCase.getFinancialTimeline(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        );

        assertTrue(timeline.events().isEmpty());
    }

    @Test
    void getFinancialTimelineRejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> financeUseCase.getFinancialTimeline(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 31)
        ));
    }

    @Test
    void getCashflowCalculatesMonthlyIncomeExpensesAndSavingsRate() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("Groceries", "1000.00", LocalDate.of(2026, 5, 2), ExpenseCategory.FOOD));

        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 5));

        assertEquals(1, cashflow.months().size());
        assertBigDecimal("3000.00", cashflow.months().get(0).expectedIncome());
        assertBigDecimal("1000.00", cashflow.months().get(0).expectedExpenses());
        assertBigDecimal("2000.00", cashflow.months().get(0).netCashflow());
        assertBigDecimal("2000.00", cashflow.months().get(0).projectedBalance());
        assertBigDecimal("2000.00", cashflow.months().get(0).savingsAmount());
        assertBigDecimal("66.67", cashflow.months().get(0).savingsRate());
    }

    @Test
    void getCashflowIncludesProjectedRecurringExpensesNotAlreadyRegistered() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("Groceries", "500.00", LocalDate.of(2026, 5, 2), ExpenseCategory.FOOD));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Rent",
                "1000.00",
                ExpenseCategory.HOUSING,
                RecurringExpenseRecurrence.MONTHLY,
                5,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 5));

        assertBigDecimal("1500.00", cashflow.months().get(0).expectedExpenses());
        assertBigDecimal("1500.00", cashflow.months().get(0).netCashflow());
    }

    @Test
    void getCashflowDoesNotDoubleCountRegisteredRecurringExpenses() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 1)));
        expenseOutputPort.expenses.add(expense("Rent", "1000.00", LocalDate.of(2026, 5, 2), ExpenseCategory.HOUSING));
        recurringExpenseOutputPort.recurringExpenses.add(recurringExpense(
                1L,
                "Rent",
                "1000.00",
                ExpenseCategory.HOUSING,
                RecurringExpenseRecurrence.MONTHLY,
                5,
                LocalDate.of(2026, 1, 1),
                null,
                true
        ));

        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 5));

        assertBigDecimal("1000.00", cashflow.months().get(0).expectedExpenses());
        assertBigDecimal("2000.00", cashflow.months().get(0).netCashflow());
    }

    @Test
    void getCashflowReturnsSafeValuesForMonthsWithoutData() {
        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 5));

        assertBigDecimal("0", cashflow.months().get(0).expectedIncome());
        assertBigDecimal("0", cashflow.months().get(0).expectedExpenses());
        assertBigDecimal("0", cashflow.months().get(0).netCashflow());
        assertBigDecimal("0", cashflow.months().get(0).projectedBalance());
        assertBigDecimal("0", cashflow.months().get(0).savingsAmount());
        assertBigDecimal("0", cashflow.months().get(0).savingsRate());
    }

    @Test
    void getCashflowCalculatesProjectedBalanceCumulatively() {
        incomeOutputPort.incomes.add(income("100.00", LocalDate.of(2026, 5, 1)));
        incomeOutputPort.incomes.add(income("50.00", LocalDate.of(2026, 6, 1)));
        expenseOutputPort.expenses.add(expense("June expense", "100.00", LocalDate.of(2026, 6, 2), ExpenseCategory.OTHER));

        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 6));

        assertEquals(2, cashflow.months().size());
        assertBigDecimal("100.00", cashflow.months().get(0).projectedBalance());
        assertBigDecimal("50.00", cashflow.months().get(1).projectedBalance());
    }

    @Test
    void getCashflowUsesZeroSavingsRateWhenIncomeIsZero() {
        expenseOutputPort.expenses.add(expense("Bill", "100.00", LocalDate.of(2026, 5, 2), ExpenseCategory.UTILITIES));

        Cashflow cashflow = financeUseCase.getCashflow(YearMonth.of(2026, 5), YearMonth.of(2026, 5));

        assertBigDecimal("0", cashflow.months().get(0).expectedIncome());
        assertBigDecimal("100.00", cashflow.months().get(0).expectedExpenses());
        assertBigDecimal("-100.00", cashflow.months().get(0).netCashflow());
        assertBigDecimal("0", cashflow.months().get(0).savingsAmount());
        assertBigDecimal("0", cashflow.months().get(0).savingsRate());
    }

    @Test
    void getCashflowRejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> financeUseCase.getCashflow(
                YearMonth.of(2026, 6),
                YearMonth.of(2026, 5)
        ));
    }

    private Income income(String amount, LocalDate date) {
        return Income.builder()
                .amount(new BigDecimal(amount))
                .incomeDate(date)
                .build();
    }

    private Income income(Long id, String source, String amount, LocalDate date) {
        return Income.builder()
                .id(id)
                .source(source)
                .amount(new BigDecimal(amount))
                .incomeDate(date)
                .build();
    }

    private Expense expense(String amount, LocalDate date, ExpenseCategory category) {
        return expense(null, amount, date, category);
    }

    private Expense expense(Long id, String concept, String amount, LocalDate date, ExpenseCategory category) {
        return Expense.builder()
                .id(id)
                .concept(concept)
                .amount(new BigDecimal(amount))
                .expenseDate(date)
                .category(category)
                .build();
    }

    private Expense expense(String concept, String amount, LocalDate date, ExpenseCategory category) {
        return Expense.builder()
                .concept(concept)
                .amount(new BigDecimal(amount))
                .expenseDate(date)
                .category(category)
                .build();
    }

    private RecurringExpense recurringExpense(
            Long id,
            String name,
            String amount,
            ExpenseCategory category,
            RecurringExpenseRecurrence recurrence,
            Integer paymentDay,
            LocalDate startDate,
            LocalDate endDate,
            Boolean active
    ) {
        return RecurringExpense.builder()
                .id(id)
                .name(name)
                .amount(new BigDecimal(amount))
                .category(category)
                .recurrence(recurrence)
                .paymentDay(paymentDay)
                .startDate(startDate)
                .endDate(endDate)
                .active(active)
                .build();
    }

    private SavingsGoal savingsGoal(
            Long id,
            String name,
            String targetAmount,
            String currentAmount,
            SavingsGoalCategory category,
            SavingsGoalStatus status,
            BigDecimal monthlySavingRate
    ) {
        return SavingsGoal.builder()
                .id(id)
                .name(name)
                .targetAmount(new BigDecimal(targetAmount))
                .currentAmount(new BigDecimal(currentAmount))
                .category(category)
                .status(status)
                .monthlySavingRate(monthlySavingRate)
                .build();
    }

    private Budget budget(
            Long id,
            YearMonth month,
            ExpenseCategory category,
            String limitAmount,
            Boolean active
    ) {
        return Budget.builder()
                .id(id)
                .month(month)
                .category(category)
                .limitAmount(new BigDecimal(limitAmount))
                .active(active)
                .build();
    }

    private void assertBreakdown(
            ExpenseCategoryBreakdown breakdown,
            ExpenseCategory category,
            String totalAmount,
            String percentage
    ) {
        assertEquals(category, breakdown.category());
        assertBigDecimal(totalAmount, breakdown.totalAmount());
        assertBigDecimal(percentage, breakdown.percentage());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static final class FakeExpenseOutputPort implements ExpenseOutputPort {

        private final List<Expense> expenses = new ArrayList<>();

        @Override
        public List<Expense> findAll() {
            return expenses;
        }

        @Override
        public List<Expense> findByCategory(ExpenseCategory category) {
            return expenses.stream()
                    .filter(expense -> category == expense.getCategory())
                    .toList();
        }

        @Override
        public List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to) {
            return expenses.stream()
                    .filter(expense -> !expense.getExpenseDate().isBefore(from))
                    .filter(expense -> !expense.getExpenseDate().isAfter(to))
                    .toList();
        }

        @Override
        public Expense save(Expense expense) {
            expenses.add(expense);
            return expense;
        }
    }

    private static final class FakeIncomeOutputPort implements IncomeOutputPort {

        private final List<Income> incomes = new ArrayList<>();

        @Override
        public List<Income> findAll() {
            return incomes;
        }

        @Override
        public List<Income> findByIncomeDateBetween(LocalDate from, LocalDate to) {
            return incomes.stream()
                    .filter(income -> !income.getIncomeDate().isBefore(from))
                    .filter(income -> !income.getIncomeDate().isAfter(to))
                    .toList();
        }

        @Override
        public Income save(Income income) {
            incomes.add(income);
            return income;
        }
    }

    private static final class FakeRecurringExpenseOutputPort implements RecurringExpenseOutputPort {

        private final List<RecurringExpense> recurringExpenses = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<RecurringExpense> findAll() {
            return recurringExpenses;
        }

        @Override
        public List<RecurringExpense> findByCategory(ExpenseCategory category) {
            return recurringExpenses.stream()
                    .filter(recurringExpense -> category == recurringExpense.getCategory())
                    .toList();
        }

        @Override
        public List<RecurringExpense> findByActive(boolean active) {
            return recurringExpenses.stream()
                    .filter(recurringExpense -> Boolean.valueOf(active).equals(recurringExpense.getActive()))
                    .toList();
        }

        @Override
        public List<RecurringExpense> findActiveWithinPeriod(LocalDate periodStart, LocalDate periodEnd) {
            return recurringExpenses;
        }

        @Override
        public Optional<RecurringExpense> findById(Long id) {
            return recurringExpenses.stream()
                    .filter(recurringExpense -> id.equals(recurringExpense.getId()))
                    .findFirst();
        }

        @Override
        public RecurringExpense save(RecurringExpense recurringExpense) {
            if (recurringExpense.getId() == null) {
                recurringExpense.setId(nextId++);
            }
            recurringExpenses.removeIf(savedRecurringExpense -> recurringExpense.getId().equals(savedRecurringExpense.getId()));
            recurringExpenses.add(recurringExpense);
            return recurringExpense;
        }

        @Override
        public void deleteById(Long id) {
            recurringExpenses.removeIf(recurringExpense -> id.equals(recurringExpense.getId()));
        }
    }

    private static final class FakeSavingsGoalOutputPort implements SavingsGoalOutputPort {

        private final List<SavingsGoal> savingsGoals = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<SavingsGoal> findAll() {
            return savingsGoals;
        }

        @Override
        public List<SavingsGoal> findByCategory(SavingsGoalCategory category) {
            return savingsGoals.stream()
                    .filter(savingsGoal -> category == savingsGoal.getCategory())
                    .toList();
        }

        @Override
        public List<SavingsGoal> findByStatus(SavingsGoalStatus status) {
            return savingsGoals.stream()
                    .filter(savingsGoal -> status == savingsGoal.getStatus())
                    .toList();
        }

        @Override
        public Optional<SavingsGoal> findById(Long id) {
            return savingsGoals.stream()
                    .filter(savingsGoal -> id.equals(savingsGoal.getId()))
                    .findFirst();
        }

        @Override
        public SavingsGoal save(SavingsGoal savingsGoal) {
            if (savingsGoal.getId() == null) {
                savingsGoal.setId(nextId++);
            }
            savingsGoals.removeIf(savedSavingsGoal -> savingsGoal.getId().equals(savedSavingsGoal.getId()));
            savingsGoals.add(savingsGoal);
            return savingsGoal;
        }

        @Override
        public void deleteById(Long id) {
            savingsGoals.removeIf(savingsGoal -> id.equals(savingsGoal.getId()));
        }
    }

    private static final class FakeBudgetOutputPort implements BudgetOutputPort {

        private final List<Budget> budgets = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<Budget> findAll() {
            return budgets;
        }

        @Override
        public List<Budget> findByMonth(YearMonth month) {
            return budgets.stream()
                    .filter(budget -> month.equals(budget.getMonth()))
                    .toList();
        }

        @Override
        public List<Budget> findByMonthAndActive(YearMonth month, boolean active) {
            return budgets.stream()
                    .filter(budget -> month.equals(budget.getMonth()))
                    .filter(budget -> Boolean.valueOf(active).equals(budget.getActive()))
                    .toList();
        }

        @Override
        public List<Budget> findByCategory(ExpenseCategory category) {
            return budgets.stream()
                    .filter(budget -> category == budget.getCategory())
                    .toList();
        }

        @Override
        public Optional<Budget> findById(Long id) {
            return budgets.stream()
                    .filter(budget -> id.equals(budget.getId()))
                    .findFirst();
        }

        @Override
        public boolean existsActiveByMonthAndCategoryExcludingId(
                YearMonth month,
                ExpenseCategory category,
                Long excludedId
        ) {
            return budgets.stream()
                    .filter(budget -> month.equals(budget.getMonth()))
                    .filter(budget -> category == budget.getCategory())
                    .filter(budget -> Boolean.TRUE.equals(budget.getActive()))
                    .anyMatch(budget -> excludedId == null || !excludedId.equals(budget.getId()));
        }

        @Override
        public Budget save(Budget budget) {
            budget.validate();
            if (budget.getActive() == null) {
                budget.setActive(Boolean.TRUE);
            }
            if (budget.getId() == null) {
                budget.setId(nextId++);
            }
            budgets.removeIf(savedBudget -> budget.getId().equals(savedBudget.getId()));
            budgets.add(budget);
            return budget;
        }

        @Override
        public void deleteById(Long id) {
            budgets.removeIf(budget -> id.equals(budget.getId()));
        }
    }
}
