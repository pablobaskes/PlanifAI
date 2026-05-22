package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.application.RecurringExpenseNotFoundException;
import com.planifai.core.finance.application.SavingsGoalNotFoundException;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.SavingsGoalOutputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.FinanceCategoryStatistic;
import com.planifai.core.finance.domain.model.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.IncomeCategory;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.ObligationPaymentStatus;
import com.planifai.core.finance.domain.model.Recurrence;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.domain.model.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.SavingsGoal;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;
import com.planifai.core.finance.domain.model.UpcomingPayment;
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
    private final RecurringExpenseOutputPort recurringExpenseOutputPort;
    private final SavingsGoalOutputPort savingsGoalOutputPort;

    public FinanceUseCase(
            ExpenseOutputPort expenseOutputPort,
            IncomeOutputPort incomeOutputPort,
            RecurringExpenseOutputPort recurringExpenseOutputPort,
            SavingsGoalOutputPort savingsGoalOutputPort
    ) {
        this.expenseOutputPort = expenseOutputPort;
        this.incomeOutputPort = incomeOutputPort;
        this.recurringExpenseOutputPort = recurringExpenseOutputPort;
        this.savingsGoalOutputPort = savingsGoalOutputPort;
    }

    @Override
    public List<Expense> getExpenses() {
        return expenseOutputPort.findAll();
    }

    @Override
    public List<Expense> getExpenses(ExpenseCategory category) {
        return category != null ? expenseOutputPort.findByCategory(category) : expenseOutputPort.findAll();
    }

    @Override
    public List<Expense> getFinanceTransactions(ExpenseCategory category) {
        return getExpenses(category);
    }

    @Override
    public Expense createExpense(Expense expense) {
        validateExpense(expense);
        expense.setId(null);
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

    @Override
    public FinanceCategoryStatistics getCategoryStatistics(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Category statistics month is required.");
        }

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(from, to);
        BigDecimal totalExpenses = sumExpenses(expenses);

        return new FinanceCategoryStatistics(
                month,
                totalExpenses,
                buildCategoryStatistics(expenses, totalExpenses)
        );
    }

    @Override
    public MonthlyObligationsSummary getMonthlyObligationsSummary(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Obligations summary month is required.");
        }

        LocalDate periodStart = month.atDay(1);
        LocalDate periodEnd = month.atEndOfMonth();
        List<Income> incomes = incomeOutputPort.findByIncomeDateBetween(periodStart, periodEnd);
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(periodStart, periodEnd);
        List<RecurringExpense> applicableRecurringExpenses = recurringExpenseOutputPort
                .findActiveWithinPeriod(periodStart, periodEnd).stream()
                .filter(recurringExpense -> appliesToMonth(recurringExpense, month))
                .toList();

        List<UpcomingPayment> upcomingPayments = applicableRecurringExpenses.stream()
                .map(recurringExpense -> toUpcomingPayment(recurringExpense, month, expenses))
                .sorted(Comparator
                        .comparing(UpcomingPayment::dueDate)
                        .thenComparing(UpcomingPayment::name)
                        .thenComparing(UpcomingPayment::recurringExpenseId))
                .toList();

        BigDecimal totalRecurringObligations = sumUpcomingPayments(upcomingPayments);
        BigDecimal pendingObligations = sumUpcomingPaymentsByStatus(upcomingPayments, ObligationPaymentStatus.PENDING);
        BigDecimal paidOrRegisteredObligations = sumUpcomingPaymentsByStatus(
                upcomingPayments,
                ObligationPaymentStatus.PAID_OR_REGISTERED
        );
        BigDecimal currentBalance = sumIncomes(incomes).subtract(sumExpenses(expenses));
        BigDecimal realAvailableMoney = currentBalance.subtract(pendingObligations);

        return new MonthlyObligationsSummary(
                month,
                totalRecurringObligations,
                pendingObligations,
                paidOrRegisteredObligations,
                realAvailableMoney,
                upcomingPayments
        );
    }

    @Override
    public List<RecurringExpense> getRecurringExpenses() {
        return recurringExpenseOutputPort.findAll();
    }

    @Override
    public List<RecurringExpense> getRecurringExpenses(ExpenseCategory category) {
        return category != null
                ? recurringExpenseOutputPort.findByCategory(category)
                : recurringExpenseOutputPort.findAll();
    }

    @Override
    public RecurringExpense createRecurringExpense(RecurringExpense recurringExpense) {
        validateRecurringExpense(recurringExpense);
        recurringExpense.setId(null);
        if (recurringExpense.getActive() == null) {
            recurringExpense.setActive(Boolean.TRUE);
        }
        return recurringExpenseOutputPort.save(recurringExpense);
    }

    @Override
    public RecurringExpense updateRecurringExpense(Long id, RecurringExpense recurringExpense) {
        if (id == null) {
            throw new IllegalArgumentException("Recurring expense id is required.");
        }
        if (recurringExpenseOutputPort.findById(id).isEmpty()) {
            throw new RecurringExpenseNotFoundException(id);
        }
        validateRecurringExpense(recurringExpense);
        recurringExpense.setId(id);
        if (recurringExpense.getActive() == null) {
            recurringExpense.setActive(Boolean.TRUE);
        }
        return recurringExpenseOutputPort.save(recurringExpense);
    }

    @Override
    public void deleteRecurringExpense(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Recurring expense id is required.");
        }
        if (recurringExpenseOutputPort.findById(id).isEmpty()) {
            throw new RecurringExpenseNotFoundException(id);
        }
        recurringExpenseOutputPort.deleteById(id);
    }

    @Override
    public List<SavingsGoal> getSavingsGoals() {
        BigDecimal currentMonthlySavingRate = calculateCurrentMonthlySavingRate();
        return savingsGoalOutputPort.findAll().stream()
                .map(savingsGoal -> withEffectiveMonthlySavingRate(savingsGoal, currentMonthlySavingRate))
                .toList();
    }

    @Override
    public SavingsGoal getSavingsGoalById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Savings goal id is required.");
        }
        SavingsGoal savingsGoal = savingsGoalOutputPort.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));
        return withEffectiveMonthlySavingRate(savingsGoal, calculateCurrentMonthlySavingRate());
    }

    @Override
    public SavingsGoal createSavingsGoal(SavingsGoal savingsGoal) {
        validateSavingsGoal(savingsGoal);
        savingsGoal.setId(null);
        applyDerivedSavingsGoalState(savingsGoal);
        SavingsGoal savedSavingsGoal = savingsGoalOutputPort.save(savingsGoal);
        return withEffectiveMonthlySavingRate(savedSavingsGoal, calculateCurrentMonthlySavingRate());
    }

    @Override
    public SavingsGoal updateSavingsGoal(Long id, SavingsGoal savingsGoal) {
        if (id == null) {
            throw new IllegalArgumentException("Savings goal id is required.");
        }
        SavingsGoal existingSavingsGoal = savingsGoalOutputPort.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));
        validateSavingsGoal(savingsGoal);
        savingsGoal.setId(id);
        savingsGoal.setCreatedAt(existingSavingsGoal.getCreatedAt());
        applyDerivedSavingsGoalState(savingsGoal);
        SavingsGoal savedSavingsGoal = savingsGoalOutputPort.save(savingsGoal);
        return withEffectiveMonthlySavingRate(savedSavingsGoal, calculateCurrentMonthlySavingRate());
    }

    @Override
    public void deleteSavingsGoal(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Savings goal id is required.");
        }
        if (savingsGoalOutputPort.findById(id).isEmpty()) {
            throw new SavingsGoalNotFoundException(id);
        }
        savingsGoalOutputPort.deleteById(id);
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
        if (expense.getCategory() == null) {
            throw new IllegalArgumentException("Expense category is required.");
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

    private void validateRecurringExpense(RecurringExpense recurringExpense) {
        if (recurringExpense == null) {
            throw new IllegalArgumentException("Recurring expense is required.");
        }
        if (recurringExpense.getName() == null || recurringExpense.getName().isBlank()) {
            throw new IllegalArgumentException("Recurring expense name is required.");
        }
        validateAmount(recurringExpense.getAmount(), "Recurring expense amount must be positive.");
        if (recurringExpense.getRecurrence() == null) {
            throw new IllegalArgumentException("Recurring expense recurrence is required.");
        }
        if (recurringExpense.getCategory() == null) {
            throw new IllegalArgumentException("Recurring expense category is required.");
        }
        if (recurringExpense.getPaymentDay() == null
                || recurringExpense.getPaymentDay() < 1
                || recurringExpense.getPaymentDay() > 31) {
            throw new IllegalArgumentException("Recurring expense payment day must be between 1 and 31.");
        }
        if (recurringExpense.getStartDate() == null) {
            throw new IllegalArgumentException("Recurring expense start date is required.");
        }
        if (recurringExpense.getEndDate() != null
                && recurringExpense.getEndDate().isBefore(recurringExpense.getStartDate())) {
            throw new IllegalArgumentException("Recurring expense end date cannot be before start date.");
        }
    }

    private void validateSavingsGoal(SavingsGoal savingsGoal) {
        if (savingsGoal == null) {
            throw new IllegalArgumentException("Savings goal is required.");
        }
        savingsGoal.validate();
    }

    private void applyDerivedSavingsGoalState(SavingsGoal savingsGoal) {
        if (savingsGoal.isCompleted()) {
            savingsGoal.setStatus(SavingsGoalStatus.COMPLETED);
        }
    }

    private SavingsGoal withEffectiveMonthlySavingRate(
            SavingsGoal savingsGoal,
            BigDecimal currentMonthlySavingRate
    ) {
        if (savingsGoal == null) {
            return null;
        }
        if (currentMonthlySavingRate != null) {
            savingsGoal.setMonthlySavingRate(currentMonthlySavingRate);
        }
        return savingsGoal;
    }

    private BigDecimal calculateCurrentMonthlySavingRate() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate periodStart = currentMonth.atDay(1);
        LocalDate periodEnd = currentMonth.atEndOfMonth();
        List<Income> incomes = incomeOutputPort.findByIncomeDateBetween(periodStart, periodEnd);
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(periodStart, periodEnd);

        if (incomes.isEmpty() && expenses.isEmpty()) {
            return null;
        }

        BigDecimal netSavings = sumIncomes(incomes).subtract(sumExpenses(expenses));
        return netSavings.compareTo(BigDecimal.ZERO) > 0 ? netSavings : BigDecimal.ZERO;
    }

    private boolean appliesToMonth(RecurringExpense recurringExpense, YearMonth month) {
        if (recurringExpense == null
                || !Boolean.TRUE.equals(recurringExpense.getActive())
                || recurringExpense.getStartDate() == null
                || recurringExpense.getPaymentDay() == null
                || recurringExpense.getPaymentDay() < 1
                || recurringExpense.getPaymentDay() > 31
                || recurringExpense.getRecurrence() == null) {
            return false;
        }

        LocalDate dueDate = projectedDueDate(recurringExpense, month);
        if (dueDate.isBefore(recurringExpense.getStartDate())) {
            return false;
        }
        if (recurringExpense.getEndDate() != null && dueDate.isAfter(recurringExpense.getEndDate())) {
            return false;
        }
        if (recurringExpense.getRecurrence() == RecurringExpenseRecurrence.YEARLY) {
            return month.getMonthValue() == recurringExpense.getStartDate().getMonthValue();
        }
        return recurringExpense.getRecurrence() == RecurringExpenseRecurrence.MONTHLY;
    }

    private UpcomingPayment toUpcomingPayment(
            RecurringExpense recurringExpense,
            YearMonth month,
            List<Expense> registeredExpenses
    ) {
        ObligationPaymentStatus status = isRegisteredExpensePresent(recurringExpense, registeredExpenses)
                ? ObligationPaymentStatus.PAID_OR_REGISTERED
                : ObligationPaymentStatus.PENDING;

        return new UpcomingPayment(
                recurringExpense.getId(),
                recurringExpense.getName(),
                recurringExpense.getAmount(),
                recurringExpense.getCategory(),
                projectedDueDate(recurringExpense, month),
                recurringExpense.getPaymentDay(),
                status
        );
    }

    private LocalDate projectedDueDate(RecurringExpense recurringExpense, YearMonth month) {
        int paymentDay = Math.min(recurringExpense.getPaymentDay(), month.lengthOfMonth());
        return month.atDay(paymentDay);
    }

    private boolean isRegisteredExpensePresent(RecurringExpense recurringExpense, List<Expense> registeredExpenses) {
        return registeredExpenses.stream()
                .anyMatch(expense -> matchesRecurringExpense(expense, recurringExpense));
    }

    private boolean matchesRecurringExpense(Expense expense, RecurringExpense recurringExpense) {
        if (expense == null || recurringExpense == null) {
            return false;
        }
        if (!sameText(expense.getConcept(), recurringExpense.getName())) {
            return false;
        }
        if (expense.getCategory() != recurringExpense.getCategory()) {
            return false;
        }
        return expense.getAmount() != null
                && recurringExpense.getAmount() != null
                && expense.getAmount().compareTo(recurringExpense.getAmount()) == 0;
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private BigDecimal sumUpcomingPayments(List<UpcomingPayment> upcomingPayments) {
        return upcomingPayments.stream()
                .map(UpcomingPayment::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumUpcomingPaymentsByStatus(
            List<UpcomingPayment> upcomingPayments,
            ObligationPaymentStatus status
    ) {
        return upcomingPayments.stream()
                .filter(upcomingPayment -> upcomingPayment.status() == status)
                .map(UpcomingPayment::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private List<FinanceCategoryStatistic> buildCategoryStatistics(List<Expense> expenses, BigDecimal totalExpenses) {
        Map<ExpenseCategory, BigDecimal> totalsByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory() != null ? expense.getCategory() : ExpenseCategory.OTHER,
                        Collectors.mapping(
                                expense -> expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return totalsByCategory.entrySet().stream()
                .map(entry -> new FinanceCategoryStatistic(
                        entry.getKey(),
                        entry.getValue(),
                        calculateExpensePercentage(entry.getValue(), totalExpenses)
                ))
                .sorted(Comparator
                        .comparing(FinanceCategoryStatistic::amount, Comparator.reverseOrder())
                        .thenComparing(statistic -> statistic.category().name()))
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
