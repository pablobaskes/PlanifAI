package com.planifai.core.finance.application.usecase;

import com.planifai.core.finance.domain.exception.BudgetNotFoundException;
import com.planifai.core.finance.domain.exception.RecurringExpenseNotFoundException;
import com.planifai.core.finance.domain.exception.SavingsGoalNotFoundException;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.application.ports.output.BudgetOutputPort;
import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.application.ports.output.SavingsGoalOutputPort;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.budget.BudgetAlert;
import com.planifai.core.finance.domain.model.budget.BudgetAlertType;
import com.planifai.core.finance.domain.model.budget.BudgetCategoryStatus;
import com.planifai.core.finance.domain.model.budget.BudgetStatus;
import com.planifai.core.finance.domain.model.budget.BudgetSummary;
import com.planifai.core.finance.domain.model.cashflow.Cashflow;
import com.planifai.core.finance.domain.model.cashflow.CashflowMonth;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.dashboard.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.dashboard.FinanceCategoryStatistic;
import com.planifai.core.finance.domain.model.dashboard.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.dashboard.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.timeline.FinancialTimeline;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEvent;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventStatus;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventType;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.domain.model.transaction.IncomeCategory;
import com.planifai.core.finance.domain.model.recurring.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.recurring.ObligationPaymentStatus;
import com.planifai.core.finance.domain.model.transaction.Recurrence;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.recurring.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.domain.model.goal.SavingsGoalStatus;
import com.planifai.core.finance.domain.model.goal.SavingsGoalsSummary;
import com.planifai.core.finance.domain.model.recurring.UpcomingPayment;
import com.planifai.core.finance.domain.FinanceConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceUseCase implements FinanceInputPort {

    private final ExpenseOutputPort expenseOutputPort;
    private final IncomeOutputPort incomeOutputPort;
    private final RecurringExpenseOutputPort recurringExpenseOutputPort;
    private final SavingsGoalOutputPort savingsGoalOutputPort;
    private final BudgetOutputPort budgetOutputPort;

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
            throw new IllegalArgumentException(FinanceConstants.DASHBOARD_MONTH_REQUIRED);
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

        return FinanceDashboard.builder()
                .month(month)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .savingsAmount(savingsAmount)
                .savingsRate(savingsRate)
                .healthStatus(healthStatus)
                .expensesByCategory(buildExpenseBreakdown(expenses, totalExpenses))
                .build();
    }

    @Override
    public FinancialTimeline getFinancialTimeline(LocalDate from, LocalDate to) {
        validateTimelineRange(from, to);

        List<FinancialTimelineEvent> events = new ArrayList<>();
        incomeOutputPort.findByIncomeDateBetween(from, to).stream()
                .map(this::toIncomeTimelineEvent)
                .forEach(events::add);
        expenseOutputPort.findByExpenseDateBetween(from, to).stream()
                .map(this::toExpenseTimelineEvent)
                .forEach(events::add);
        recurringExpenseOutputPort.findActiveWithinPeriod(from, to).stream()
                .flatMap(recurringExpense -> projectRecurringExpenseTimelineEvents(recurringExpense, from, to).stream())
                .forEach(events::add);

        List<FinancialTimelineEvent> sortedEvents = events.stream()
                .sorted(Comparator
                        .comparing(FinancialTimelineEvent::date)
                        .thenComparing(event -> event.type().name())
                        .thenComparing(FinancialTimelineEvent::label)
                        .thenComparing(FinancialTimelineEvent::id))
                .toList();

        return FinancialTimeline.builder()
                .from(from)
                .to(to)
                .events(sortedEvents)
                .build();
    }

    @Override
    public Cashflow getCashflow(YearMonth from, YearMonth to) {
        validateCashflowRange(from, to);

        YearMonth currentMonth = from;
        BigDecimal projectedBalance = BigDecimal.ZERO;
        List<CashflowMonth> months = new ArrayList<>();

        while (!currentMonth.isAfter(to)) {
            CashflowMonth cashflowMonth = calculateCashflowMonth(currentMonth, projectedBalance);
            months.add(cashflowMonth);
            projectedBalance = cashflowMonth.projectedBalance();
            currentMonth = currentMonth.plusMonths(1);
        }

        return Cashflow.builder()
                .from(from)
                .to(to)
                .months(months)
                .build();
    }

    @Override
    public FinanceCategoryStatistics getCategoryStatistics(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.CATEGORY_STATISTICS_MONTH_REQUIRED);
        }

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(from, to);
        BigDecimal totalExpenses = sumExpenses(expenses);

        return FinanceCategoryStatistics.builder()
                .month(month)
                .totalExpenses(totalExpenses)
                .categories(buildCategoryStatistics(expenses, totalExpenses))
                .build();
    }

    @Override
    public MonthlyObligationsSummary getMonthlyObligationsSummary(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.OBLIGATIONS_SUMMARY_MONTH_REQUIRED);
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

        return MonthlyObligationsSummary.builder()
                .month(month)
                .totalRecurringObligations(totalRecurringObligations)
                .pendingObligations(pendingObligations)
                .paidOrRegisteredObligations(paidOrRegisteredObligations)
                .realAvailableMoney(realAvailableMoney)
                .upcomingPayments(upcomingPayments)
                .build();
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
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_ID_REQUIRED);
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
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_ID_REQUIRED);
        }
        if (recurringExpenseOutputPort.findById(id).isEmpty()) {
            throw new RecurringExpenseNotFoundException(id);
        }
        recurringExpenseOutputPort.deleteById(id);
    }

    @Override
    public List<Budget> getBudgets(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_MONTH_REQUIRED);
        }
        return budgetOutputPort.findByMonth(month);
    }

    @Override
    public BudgetSummary getBudgetSummary(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_MONTH_REQUIRED);
        }

        List<Budget> activeBudgets = budgetOutputPort.findByMonthAndActive(month, true);
        LocalDate periodStart = month.atDay(1);
        LocalDate periodEnd = month.atEndOfMonth();
        Map<ExpenseCategory, BigDecimal> expensesByCategory = expenseOutputPort
                .findByExpenseDateBetween(periodStart, periodEnd)
                .stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory() != null ? expense.getCategory() : ExpenseCategory.OTHER,
                        Collectors.mapping(
                                expense -> expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        List<BudgetCategoryStatus> categories = activeBudgets.stream()
                .map(budget -> toBudgetCategoryStatus(budget, expensesByCategory))
                .sorted(Comparator
                        .comparing(BudgetCategoryStatus::category)
                        .thenComparing(BudgetCategoryStatus::budgetId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BigDecimal totalLimitAmount = sumBudgetLimits(categories);
        BigDecimal totalConsumedAmount = sumBudgetConsumed(categories);
        BigDecimal totalRemainingAmount = sumBudgetRemaining(categories);
        BigDecimal totalOverspentAmount = sumBudgetOverspent(categories);
        BigDecimal overallConsumptionPercentage = calculateBudgetConsumptionPercentage(
                totalConsumedAmount,
                totalLimitAmount
        );
        BudgetStatus status = calculateBudgetStatus(overallConsumptionPercentage);
        List<BudgetAlert> alerts = categories.stream()
                .flatMap(category -> category.alerts().stream())
                .toList();

        return BudgetSummary.builder()
                .month(month)
                .totalLimitAmount(totalLimitAmount)
                .totalConsumedAmount(totalConsumedAmount)
                .totalRemainingAmount(totalRemainingAmount)
                .totalOverspentAmount(totalOverspentAmount)
                .overallConsumptionPercentage(overallConsumptionPercentage)
                .status(status)
                .categories(categories)
                .alerts(alerts)
                .build();
    }

    @Override
    public Budget getBudgetById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_ID_REQUIRED);
        }
        return budgetOutputPort.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
    }

    @Override
    public Budget createBudget(Budget budget) {
        validateBudget(budget);
        budget.setId(null);
        applyBudgetDefaults(budget);
        validateActiveBudgetIsUnique(budget);
        return budgetOutputPort.save(budget);
    }

    @Override
    public Budget updateBudget(Long id, Budget budget) {
        if (id == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_ID_REQUIRED);
        }
        if (budgetOutputPort.findById(id).isEmpty()) {
            throw new BudgetNotFoundException(id);
        }
        validateBudget(budget);
        budget.setId(id);
        applyBudgetDefaults(budget);
        validateActiveBudgetIsUnique(budget);
        return budgetOutputPort.save(budget);
    }

    @Override
    public void deleteBudget(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_ID_REQUIRED);
        }
        if (budgetOutputPort.findById(id).isEmpty()) {
            throw new BudgetNotFoundException(id);
        }
        budgetOutputPort.deleteById(id);
    }

    @Override
    public List<SavingsGoal> getSavingsGoals() {
        BigDecimal currentMonthlySavingRate = calculateCurrentMonthlySavingRate();
        return savingsGoalOutputPort.findAll().stream()
                .map(savingsGoal -> withEffectiveMonthlySavingRate(savingsGoal, currentMonthlySavingRate))
                .toList();
    }

    @Override
    public SavingsGoalsSummary getSavingsGoalsSummary() {
        BigDecimal currentMonthlySavingRate = calculateCurrentMonthlySavingRate();
        List<SavingsGoal> savingsGoals = savingsGoalOutputPort.findAll().stream()
                .map(savingsGoal -> withEffectiveMonthlySavingRate(savingsGoal, currentMonthlySavingRate))
                .toList();
        List<SavingsGoal> nonCancelledGoals = savingsGoals.stream()
                .filter(savingsGoal -> savingsGoal.getStatus() != SavingsGoalStatus.CANCELLED)
                .toList();

        BigDecimal totalTargetAmount = sumSavingsGoalTargets(nonCancelledGoals);
        BigDecimal totalCurrentAmount = sumSavingsGoalCurrentAmounts(nonCancelledGoals);
        BigDecimal totalRemainingAmount = sumSavingsGoalRemainingAmounts(nonCancelledGoals);
        BigDecimal monthlySavingRate = calculateSummaryMonthlySavingRate(nonCancelledGoals, currentMonthlySavingRate);
        Integer estimatedMonthsToCompletion = calculateSummaryEstimatedMonths(
                totalRemainingAmount,
                monthlySavingRate
        );

        return SavingsGoalsSummary.builder()
                .totalGoals(savingsGoals.size())
                .activeGoals(countSavingsGoalsByStatus(savingsGoals, SavingsGoalStatus.ACTIVE))
                .completedGoals(countSavingsGoalsByStatus(savingsGoals, SavingsGoalStatus.COMPLETED))
                .pausedGoals(countSavingsGoalsByStatus(savingsGoals, SavingsGoalStatus.PAUSED))
                .cancelledGoals(countSavingsGoalsByStatus(savingsGoals, SavingsGoalStatus.CANCELLED))
                .totalTargetAmount(totalTargetAmount)
                .totalCurrentAmount(totalCurrentAmount)
                .totalRemainingAmount(totalRemainingAmount)
                .overallProgressPercentage(calculateOverallSavingsGoalProgress(totalCurrentAmount, totalTargetAmount))
                .monthlySavingRate(monthlySavingRate)
                .estimatedMonthsToCompletion(estimatedMonthsToCompletion)
                .estimatedCompletionDate(calculateSummaryEstimatedCompletionDate(estimatedMonthsToCompletion))
                .nearestGoalToComplete(findNearestGoalToComplete(savingsGoals))
                .build();
    }

    @Override
    public SavingsGoal getSavingsGoalById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_ID_REQUIRED);
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
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_ID_REQUIRED);
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
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_ID_REQUIRED);
        }
        if (savingsGoalOutputPort.findById(id).isEmpty()) {
            throw new SavingsGoalNotFoundException(id);
        }
        savingsGoalOutputPort.deleteById(id);
    }

    private void validateExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException(FinanceConstants.EXPENSE_REQUIRED);
        }
        if (expense.getConcept() == null || expense.getConcept().isBlank()) {
            throw new IllegalArgumentException(FinanceConstants.EXPENSE_CONCEPT_REQUIRED);
        }
        validateAmount(expense.getAmount(), FinanceConstants.EXPENSE_AMOUNT_POSITIVE);
        if (expense.getExpenseDate() == null) {
            throw new IllegalArgumentException(FinanceConstants.EXPENSE_DATE_REQUIRED);
        }
        if (expense.getCategory() == null) {
            throw new IllegalArgumentException(FinanceConstants.EXPENSE_CATEGORY_REQUIRED);
        }
    }

    private void validateIncome(Income income) {
        if (income == null) {
            throw new IllegalArgumentException(FinanceConstants.INCOME_REQUIRED);
        }
        if (income.getSource() == null || income.getSource().isBlank()) {
            throw new IllegalArgumentException(FinanceConstants.INCOME_SOURCE_REQUIRED);
        }
        validateAmount(income.getAmount(), FinanceConstants.INCOME_AMOUNT_POSITIVE);
        if (income.getIncomeDate() == null) {
            throw new IllegalArgumentException(FinanceConstants.INCOME_DATE_REQUIRED);
        }
    }

    private void validateRecurringExpense(RecurringExpense recurringExpense) {
        if (recurringExpense == null) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_REQUIRED);
        }
        if (recurringExpense.getName() == null || recurringExpense.getName().isBlank()) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_NAME_REQUIRED);
        }
        validateAmount(recurringExpense.getAmount(), FinanceConstants.RECURRING_EXPENSE_AMOUNT_POSITIVE);
        if (recurringExpense.getRecurrence() == null) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_RECURRENCE_REQUIRED);
        }
        if (recurringExpense.getCategory() == null) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_CATEGORY_REQUIRED);
        }
        if (recurringExpense.getPaymentDay() == null
                || recurringExpense.getPaymentDay() < FinanceConstants.MIN_PAYMENT_DAY
                || recurringExpense.getPaymentDay() > FinanceConstants.MAX_PAYMENT_DAY) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_PAYMENT_DAY_RANGE);
        }
        if (recurringExpense.getStartDate() == null) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_START_DATE_REQUIRED);
        }
        if (recurringExpense.getEndDate() != null
                && recurringExpense.getEndDate().isBefore(recurringExpense.getStartDate())) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_END_DATE_BEFORE_START);
        }
    }

    private void validateSavingsGoal(SavingsGoal savingsGoal) {
        if (savingsGoal == null) {
            throw new IllegalArgumentException(FinanceConstants.SAVINGS_GOAL_REQUIRED);
        }
        savingsGoal.validate();
    }

    private void validateBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_REQUIRED);
        }
        budget.validate();
    }

    private void validateTimelineRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(FinanceConstants.TIMELINE_RANGE_REQUIRED);
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(FinanceConstants.TIMELINE_RANGE_INVALID);
        }
    }

    private void validateCashflowRange(YearMonth from, YearMonth to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(FinanceConstants.CASHFLOW_RANGE_REQUIRED);
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(FinanceConstants.CASHFLOW_RANGE_INVALID);
        }
    }

    private void applyBudgetDefaults(Budget budget) {
        if (budget.getActive() == null) {
            budget.setActive(Boolean.TRUE);
        }
    }

    private void validateActiveBudgetIsUnique(Budget budget) {
        if (Boolean.TRUE.equals(budget.getActive())
                && budgetOutputPort.existsActiveByMonthAndCategoryExcludingId(
                        budget.getMonth(),
                        budget.getCategory(),
                        budget.getId()
                )) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_ACTIVE_DUPLICATE);
        }
    }

    private BudgetCategoryStatus toBudgetCategoryStatus(
            Budget budget,
            Map<ExpenseCategory, BigDecimal> expensesByCategory
    ) {
        BigDecimal limitAmount = budget.getLimitAmount() != null ? budget.getLimitAmount() : BigDecimal.ZERO;
        BigDecimal consumedAmount = expensesByCategory.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
        BigDecimal difference = limitAmount.subtract(consumedAmount);
        BigDecimal remainingAmount = difference.compareTo(BigDecimal.ZERO) > 0 ? difference : BigDecimal.ZERO;
        BigDecimal overspentAmount = difference.compareTo(BigDecimal.ZERO) < 0
                ? consumedAmount.subtract(limitAmount)
                : BigDecimal.ZERO;
        BigDecimal consumptionPercentage = calculateBudgetConsumptionPercentage(consumedAmount, limitAmount);
        BudgetStatus status = calculateBudgetStatus(consumptionPercentage);

        return BudgetCategoryStatus.builder()
                .budgetId(budget.getId())
                .category(budget.getCategory())
                .limitAmount(limitAmount)
                .consumedAmount(consumedAmount)
                .remainingAmount(remainingAmount)
                .overspentAmount(overspentAmount)
                .consumptionPercentage(consumptionPercentage)
                .status(status)
                .alerts(buildBudgetAlerts(
                        budget.getCategory(),
                        limitAmount,
                        consumedAmount,
                        consumptionPercentage,
                        status
                ))
                .build();
    }

    private BigDecimal calculateBudgetConsumptionPercentage(BigDecimal consumedAmount, BigDecimal limitAmount) {
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeConsumedAmount = consumedAmount != null ? consumedAmount : BigDecimal.ZERO;
        return safeConsumedAmount
                .multiply(FinanceConstants.MAX_PERCENTAGE)
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
    }

    private BudgetStatus calculateBudgetStatus(BigDecimal consumptionPercentage) {
        if (consumptionPercentage.compareTo(FinanceConstants.MAX_PERCENTAGE) > 0) {
            return BudgetStatus.EXCEEDED;
        }
        if (consumptionPercentage.compareTo(FinanceConstants.BUDGET_WARNING_PERCENTAGE) >= 0) {
            return BudgetStatus.WARNING;
        }
        return BudgetStatus.OK;
    }

    private List<BudgetAlert> buildBudgetAlerts(
            ExpenseCategory category,
            BigDecimal limitAmount,
            BigDecimal consumedAmount,
            BigDecimal consumptionPercentage,
            BudgetStatus status
    ) {
        if (status == BudgetStatus.OK) {
            return List.of();
        }

        BudgetAlertType type = status == BudgetStatus.EXCEEDED
                ? BudgetAlertType.BUDGET_EXCEEDED
                : BudgetAlertType.APPROACHING_LIMIT;
        BigDecimal threshold = status == BudgetStatus.EXCEEDED
                ? FinanceConstants.MAX_PERCENTAGE
                : FinanceConstants.BUDGET_WARNING_PERCENTAGE;
        String message = status == BudgetStatus.EXCEEDED
                ? "Budget exceeded for category " + category.name() + "."
                : "Budget approaching limit for category " + category.name() + ".";

        return List.of(BudgetAlert.builder()
                .type(type)
                .category(category)
                .limitAmount(limitAmount)
                .consumedAmount(consumedAmount)
                .threshold(threshold)
                .message(message)
                .build());
    }

    private BigDecimal sumBudgetLimits(List<BudgetCategoryStatus> categories) {
        return categories.stream()
                .map(BudgetCategoryStatus::limitAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBudgetConsumed(List<BudgetCategoryStatus> categories) {
        return categories.stream()
                .map(BudgetCategoryStatus::consumedAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBudgetRemaining(List<BudgetCategoryStatus> categories) {
        return categories.stream()
                .map(BudgetCategoryStatus::remainingAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBudgetOverspent(List<BudgetCategoryStatus> categories) {
        return categories.stream()
                .map(BudgetCategoryStatus::overspentAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private Integer countSavingsGoalsByStatus(List<SavingsGoal> savingsGoals, SavingsGoalStatus status) {
        return (int) savingsGoals.stream()
                .filter(savingsGoal -> savingsGoal.getStatus() == status)
                .count();
    }

    private BigDecimal sumSavingsGoalTargets(List<SavingsGoal> savingsGoals) {
        return savingsGoals.stream()
                .map(SavingsGoal::getTargetAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSavingsGoalCurrentAmounts(List<SavingsGoal> savingsGoals) {
        return savingsGoals.stream()
                .map(SavingsGoal::getCurrentAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSavingsGoalRemainingAmounts(List<SavingsGoal> savingsGoals) {
        return savingsGoals.stream()
                .map(SavingsGoal::remainingAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateOverallSavingsGoalProgress(
            BigDecimal totalCurrentAmount,
            BigDecimal totalTargetAmount
    ) {
        if (totalTargetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal progress = totalCurrentAmount
                .multiply(FinanceConstants.MAX_PERCENTAGE)
                .divide(totalTargetAmount, 2, RoundingMode.HALF_UP);
        return progress.compareTo(FinanceConstants.MAX_PERCENTAGE) > 0 ? FinanceConstants.MAX_PERCENTAGE : progress;
    }

    private BigDecimal calculateSummaryMonthlySavingRate(
            List<SavingsGoal> nonCancelledGoals,
            BigDecimal currentMonthlySavingRate
    ) {
        if (currentMonthlySavingRate != null) {
            return currentMonthlySavingRate;
        }
        return nonCancelledGoals.stream()
                .filter(savingsGoal -> savingsGoal.getStatus() == SavingsGoalStatus.ACTIVE)
                .map(SavingsGoal::getMonthlySavingRate)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculateSummaryEstimatedMonths(BigDecimal totalRemainingAmount, BigDecimal monthlySavingRate) {
        if (totalRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        if (monthlySavingRate == null || monthlySavingRate.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return totalRemainingAmount
                .divide(monthlySavingRate, 0, RoundingMode.CEILING)
                .intValue();
    }

    private LocalDate calculateSummaryEstimatedCompletionDate(Integer estimatedMonthsToCompletion) {
        return estimatedMonthsToCompletion != null ? LocalDate.now().plusMonths(estimatedMonthsToCompletion) : null;
    }

    private SavingsGoal findNearestGoalToComplete(List<SavingsGoal> savingsGoals) {
        return savingsGoals.stream()
                .filter(savingsGoal -> savingsGoal.getStatus() == SavingsGoalStatus.ACTIVE)
                .filter(savingsGoal -> savingsGoal.remainingAmount().compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator
                        .comparing(SavingsGoal::remainingAmount)
                        .thenComparing(SavingsGoal::progressPercentage, Comparator.reverseOrder())
                        .thenComparing(SavingsGoal::getName))
                .orElse(null);
    }

    private boolean appliesToMonth(RecurringExpense recurringExpense, YearMonth month) {
        if (recurringExpense == null
                || !Boolean.TRUE.equals(recurringExpense.getActive())
                || recurringExpense.getStartDate() == null
                || recurringExpense.getPaymentDay() == null
                || recurringExpense.getPaymentDay() < FinanceConstants.MIN_PAYMENT_DAY
                || recurringExpense.getPaymentDay() > FinanceConstants.MAX_PAYMENT_DAY
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

    private FinancialTimelineEvent toIncomeTimelineEvent(Income income) {
        return FinancialTimelineEvent.builder()
                .id("income-" + income.getId())
                .date(income.getIncomeDate())
                .type(FinancialTimelineEventType.INCOME)
                .label(income.getSource())
                .amount(income.getAmount() != null ? income.getAmount() : BigDecimal.ZERO)
                .category(null)
                .source("income")
                .projected(false)
                .status(FinancialTimelineEventStatus.POSTED)
                .build();
    }

    private FinancialTimelineEvent toExpenseTimelineEvent(Expense expense) {
        BigDecimal amount = expense.getAmount() != null ? expense.getAmount().negate() : BigDecimal.ZERO;
        return FinancialTimelineEvent.builder()
                .id("expense-" + expense.getId())
                .date(expense.getExpenseDate())
                .type(FinancialTimelineEventType.EXPENSE)
                .label(expense.getConcept())
                .amount(amount)
                .category(expense.getCategory())
                .source("expense")
                .projected(false)
                .status(FinancialTimelineEventStatus.POSTED)
                .build();
    }

    private List<FinancialTimelineEvent> projectRecurringExpenseTimelineEvents(
            RecurringExpense recurringExpense,
            LocalDate from,
            LocalDate to
    ) {
        YearMonth currentMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);
        List<FinancialTimelineEvent> events = new ArrayList<>();

        while (!currentMonth.isAfter(endMonth)) {
            if (appliesToMonth(recurringExpense, currentMonth)) {
                LocalDate dueDate = projectedDueDate(recurringExpense, currentMonth);
                if (!dueDate.isBefore(from) && !dueDate.isAfter(to)) {
                    events.add(toRecurringExpenseTimelineEvent(recurringExpense, dueDate));
                }
            }
            currentMonth = currentMonth.plusMonths(1);
        }

        return events;
    }

    private CashflowMonth calculateCashflowMonth(YearMonth month, BigDecimal previousProjectedBalance) {
        LocalDate periodStart = month.atDay(1);
        LocalDate periodEnd = month.atEndOfMonth();
        List<Income> incomes = incomeOutputPort.findByIncomeDateBetween(periodStart, periodEnd);
        List<Expense> expenses = expenseOutputPort.findByExpenseDateBetween(periodStart, periodEnd);
        List<RecurringExpense> recurringExpenses = recurringExpenseOutputPort
                .findActiveWithinPeriod(periodStart, periodEnd).stream()
                .filter(recurringExpense -> appliesToMonth(recurringExpense, month))
                .toList();

        BigDecimal expectedIncome = sumIncomes(incomes);
        BigDecimal realExpenses = sumExpenses(expenses);
        BigDecimal projectedRecurringExpenses = sumProjectedRecurringExpensesNotRegistered(
                recurringExpenses,
                expenses
        );
        BigDecimal expectedExpenses = realExpenses.add(projectedRecurringExpenses);
        BigDecimal netCashflow = expectedIncome.subtract(expectedExpenses);
        BigDecimal projectedBalance = previousProjectedBalance.add(netCashflow);
        BigDecimal savingsAmount = netCashflow.compareTo(BigDecimal.ZERO) > 0 ? netCashflow : BigDecimal.ZERO;

        return CashflowMonth.builder()
                .month(month)
                .expectedIncome(expectedIncome)
                .expectedExpenses(expectedExpenses)
                .projectedBalance(projectedBalance)
                .netCashflow(netCashflow)
                .savingsAmount(savingsAmount)
                .savingsRate(calculateSavingsRate(savingsAmount, expectedIncome))
                .build();
    }

    private BigDecimal sumProjectedRecurringExpensesNotRegistered(
            List<RecurringExpense> recurringExpenses,
            List<Expense> registeredExpenses
    ) {
        return recurringExpenses.stream()
                .filter(recurringExpense -> !isRegisteredExpensePresent(recurringExpense, registeredExpenses))
                .map(RecurringExpense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FinancialTimelineEvent toRecurringExpenseTimelineEvent(
            RecurringExpense recurringExpense,
            LocalDate dueDate
    ) {
        BigDecimal amount = recurringExpense.getAmount() != null
                ? recurringExpense.getAmount().negate()
                : BigDecimal.ZERO;
        return FinancialTimelineEvent.builder()
                .id("recurring-expense-" + recurringExpense.getId() + "-" + dueDate)
                .date(dueDate)
                .type(FinancialTimelineEventType.RECURRING_EXPENSE)
                .label(recurringExpense.getName())
                .amount(amount)
                .category(recurringExpense.getCategory())
                .source("recurring-expense")
                .projected(true)
                .status(FinancialTimelineEventStatus.PROJECTED)
                .build();
    }

    private UpcomingPayment toUpcomingPayment(
            RecurringExpense recurringExpense,
            YearMonth month,
            List<Expense> registeredExpenses
    ) {
        ObligationPaymentStatus status = isRegisteredExpensePresent(recurringExpense, registeredExpenses)
                ? ObligationPaymentStatus.PAID_OR_REGISTERED
                : ObligationPaymentStatus.PENDING;

        return UpcomingPayment.builder()
                .recurringExpenseId(recurringExpense.getId())
                .name(recurringExpense.getName())
                .amount(recurringExpense.getAmount())
                .category(recurringExpense.getCategory())
                .dueDate(projectedDueDate(recurringExpense, month))
                .paymentDay(recurringExpense.getPaymentDay())
                .status(status)
                .build();
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
                .multiply(FinanceConstants.MAX_PERCENTAGE)
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
        if (netBalance.compareTo(BigDecimal.ZERO) > 0
                && savingsRate.compareTo(FinanceConstants.GOOD_SAVINGS_RATE_THRESHOLD) >= 0) {
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
                .map(entry -> ExpenseCategoryBreakdown.builder()
                        .category(entry.getKey())
                        .totalAmount(entry.getValue())
                        .percentage(calculateExpensePercentage(entry.getValue(), totalExpenses))
                        .build())
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
                .map(entry -> FinanceCategoryStatistic.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .percentage(calculateExpensePercentage(entry.getValue(), totalExpenses))
                        .build())
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
                .multiply(FinanceConstants.MAX_PERCENTAGE)
                .divide(totalExpenses, 2, RoundingMode.HALF_UP);
    }
}
