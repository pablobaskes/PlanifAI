package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.RecurringExpense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private final FakeExpenseOutputPort expenseOutputPort = new FakeExpenseOutputPort();
    private final FakeIncomeOutputPort incomeOutputPort = new FakeIncomeOutputPort();
    private final FakeRecurringExpenseOutputPort recurringExpenseOutputPort = new FakeRecurringExpenseOutputPort();
    private final FinanceUseCase financeUseCase = new FinanceUseCase(
            expenseOutputPort,
            incomeOutputPort,
            recurringExpenseOutputPort
    );

    @Test
    void getDashboardCalculatesMonthlySummarySavingsGoodHealthAndBreakdown() {
        incomeOutputPort.incomes.add(income("3000.00", LocalDate.of(2026, 5, 5)));
        incomeOutputPort.incomes.add(income("999.00", LocalDate.of(2026, 6, 1)));
        expenseOutputPort.expenses.add(expense("1000.00", LocalDate.of(2026, 5, 10), ExpenseCategory.MORTGAGE));
        expenseOutputPort.expenses.add(expense("500.00", LocalDate.of(2026, 5, 12), ExpenseCategory.GROCERIES));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 4, 30), ExpenseCategory.LEISURE));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertBigDecimal("3000.00", dashboard.totalIncome());
        assertBigDecimal("1500.00", dashboard.totalExpenses());
        assertBigDecimal("1500.00", dashboard.netBalance());
        assertBigDecimal("1500.00", dashboard.savingsAmount());
        assertBigDecimal("50.00", dashboard.savingsRate());
        assertEquals(FinanceHealthStatus.GOOD, dashboard.healthStatus());
        assertEquals(2, dashboard.expensesByCategory().size());
        assertBreakdown(dashboard.expensesByCategory().get(0), ExpenseCategory.MORTGAGE, "1000.00", "66.67");
        assertBreakdown(dashboard.expensesByCategory().get(1), ExpenseCategory.GROCERIES, "500.00", "33.33");
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
        expenseOutputPort.expenses.add(expense("100.00", LocalDate.of(2026, 5, 2), ExpenseCategory.GROCERIES));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 5, 3), ExpenseCategory.GROCERIES));
        expenseOutputPort.expenses.add(expense("50.00", LocalDate.of(2026, 5, 4), null));

        FinanceDashboard dashboard = financeUseCase.getDashboard(YearMonth.of(2026, 5));

        assertEquals(2, dashboard.expensesByCategory().size());
        assertBreakdown(dashboard.expensesByCategory().get(0), ExpenseCategory.GROCERIES, "150.00", "75.00");
        assertBreakdown(dashboard.expensesByCategory().get(1), ExpenseCategory.OTHER, "50.00", "25.00");
    }

    private Income income(String amount, LocalDate date) {
        Income income = new Income();
        income.setAmount(new BigDecimal(amount));
        income.setIncomeDate(date);
        return income;
    }

    private Expense expense(String amount, LocalDate date, ExpenseCategory category) {
        Expense expense = new Expense();
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);
        expense.setCategory(category);
        return expense;
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

        @Override
        public List<RecurringExpense> findAll() {
            return recurringExpenses;
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
            recurringExpenses.add(recurringExpense);
            return recurringExpense;
        }

        @Override
        public void deleteById(Long id) {
            recurringExpenses.removeIf(recurringExpense -> id.equals(recurringExpense.getId()));
        }
    }
}
