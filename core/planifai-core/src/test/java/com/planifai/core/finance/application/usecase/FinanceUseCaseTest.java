package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.SavingsGoalOutputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.ObligationPaymentStatus;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.domain.model.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.SavingsGoal;
import com.planifai.core.finance.domain.model.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;
import com.planifai.core.finance.domain.model.SavingsGoalsSummary;
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
    private final FinanceUseCase financeUseCase = new FinanceUseCase(
            expenseOutputPort,
            incomeOutputPort,
            recurringExpenseOutputPort,
            savingsGoalOutputPort
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

    private Income income(String amount, LocalDate date) {
        Income income = new Income();
        income.setAmount(new BigDecimal(amount));
        income.setIncomeDate(date);
        return income;
    }

    private Expense expense(String amount, LocalDate date, ExpenseCategory category) {
        return expense(null, amount, date, category);
    }

    private Expense expense(String concept, String amount, LocalDate date, ExpenseCategory category) {
        Expense expense = new Expense();
        expense.setConcept(concept);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);
        expense.setCategory(category);
        return expense;
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
        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setId(id);
        recurringExpense.setName(name);
        recurringExpense.setAmount(new BigDecimal(amount));
        recurringExpense.setCategory(category);
        recurringExpense.setRecurrence(recurrence);
        recurringExpense.setPaymentDay(paymentDay);
        recurringExpense.setStartDate(startDate);
        recurringExpense.setEndDate(endDate);
        recurringExpense.setActive(active);
        return recurringExpense;
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
        SavingsGoal savingsGoal = new SavingsGoal();
        savingsGoal.setId(id);
        savingsGoal.setName(name);
        savingsGoal.setTargetAmount(new BigDecimal(targetAmount));
        savingsGoal.setCurrentAmount(new BigDecimal(currentAmount));
        savingsGoal.setCategory(category);
        savingsGoal.setStatus(status);
        savingsGoal.setMonthlySavingRate(monthlySavingRate);
        return savingsGoal;
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
}
