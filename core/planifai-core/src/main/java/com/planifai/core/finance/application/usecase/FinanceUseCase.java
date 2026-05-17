package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.IncomeCategory;
import com.planifai.core.finance.domain.model.Recurrence;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceUseCase implements FinanceInputPort {

    private final ExpenseOutputPort expenseOutputPort;
    private final IncomeOutputPort incomeOutputPort;

    public FinanceUseCase(ExpenseOutputPort expenseOutputPort, IncomeOutputPort incomeOutputPort) {
        this.expenseOutputPort = expenseOutputPort;
        this.incomeOutputPort = incomeOutputPort;
    }

    @Override
    public List<Expense> getExpenses() {
        return expenseOutputPort.findAll();
    }

    @Override
    public Expense createExpense(Expense expense) {
        validateExpense(expense);
        expense.setId(null);
        if (expense.getCategory() == null) {
            expense.setCategory(ExpenseCategory.OTHER);
        }
        if (expense.getRecurrence() == null) {
            expense.setRecurrence(Recurrence.ONE_OFF);
        }
        return expenseOutputPort.save(expense);
    }

    @Override
    public List<Income> getIncomes() {
        return incomeOutputPort.findAll();
    }

    @Override
    public Income createIncome(Income income) {
        validateIncome(income);
        income.setId(null);
        if (income.getCategory() == null) {
            income.setCategory(IncomeCategory.OTHER);
        }
        if (income.getRecurrence() == null) {
            income.setRecurrence(Recurrence.ONE_OFF);
        }
        return incomeOutputPort.save(income);
    }

    @Override
    public FinanceDashboard getDashboard(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Dashboard month is required.");
        }

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Income> incomes = incomeOutputPort.findByIncomeDateBetween(from, to);
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(from, to);

        BigDecimal totalIncome = sumIncomes(incomes);
        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        BigDecimal savingsAmount = netBalance;
        BigDecimal savingsRate = calculateSavingsRate(savingsAmount, totalIncome);
        FinanceHealthStatus healthStatus = calculateHealthStatus(totalIncome, totalExpenses, netBalance, savingsRate);

        return new FinanceDashboard(
                month,
                totalIncome,
                totalExpenses,
                netBalance,
                savingsAmount,
                savingsRate,
                healthStatus,
                buildExpenseBreakdown(expenses, totalExpenses)
        );
    }

    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense is required.");
        }
        if (expense.getConcept() == null || expense.getConcept().isBlank()) {
            throw new IllegalArgumentException("Expense concept is required.");
        }
        validateAmount(expense.getAmount(), "Expense amount must be positive.");
        if (expense.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required.");
        }
    }

    private void validateIncome(Income income) {
        if (income == null) {
            throw new IllegalArgumentException("Income is required.");
        }
        if (income.getSource() == null || income.getSource().isBlank()) {
            throw new IllegalArgumentException("Income source is required.");
        }
        validateAmount(income.getAmount(), "Income amount must be positive.");
        if (income.getIncomeDate() == null) {
            throw new IllegalArgumentException("Income date is required.");
        }
    }

    private void validateAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal sumIncomes(List<Income> incomes) {
        return incomes.stream()
                .map(Income::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSavingsRate(BigDecimal savingsAmount, BigDecimal totalIncome) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return savingsAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(totalIncome, 2, RoundingMode.HALF_UP);
    }

    private FinanceHealthStatus calculateHealthStatus(
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal netBalance,
            BigDecimal savingsRate
    ) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0 && totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return FinanceHealthStatus.NO_DATA;
        }
        if (netBalance.compareTo(BigDecimal.ZERO) > 0 && savingsRate.compareTo(BigDecimal.valueOf(20)) >= 0) {
            return FinanceHealthStatus.GOOD;
        }
        if (netBalance.compareTo(BigDecimal.ZERO) >= 0) {
            return FinanceHealthStatus.WARNING;
        }
        return FinanceHealthStatus.BAD;
    }

    private List<ExpenseCategoryBreakdown> buildExpenseBreakdown(List<Expense> expenses, BigDecimal totalExpenses) {
        Map<ExpenseCategory, BigDecimal> totalsByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory() != null ? expense.getCategory() : ExpenseCategory.OTHER,
                        Collectors.mapping(
                                expense -> expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return totalsByCategory.entrySet().stream()
                .map(entry -> new ExpenseCategoryBreakdown(
                        entry.getKey(),
                        entry.getValue(),
                        calculateExpensePercentage(entry.getValue(), totalExpenses)
                ))
                .sorted(Comparator
                        .comparing(ExpenseCategoryBreakdown::totalAmount, Comparator.reverseOrder())
                        .thenComparing(breakdown -> breakdown.category().name()))
                .toList();
    }

    private BigDecimal calculateExpensePercentage(BigDecimal categoryTotal, BigDecimal totalExpenses) {
        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return categoryTotal
                .multiply(BigDecimal.valueOf(100))
                .divide(totalExpenses, 2, RoundingMode.HALF_UP);
    }
}
