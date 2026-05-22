package com.planifai.core.finance.infrastructure.input.rest.mapper;

import com.planifai.core.dto.BudgetRequest;
import com.planifai.core.dto.BudgetResponse;
import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceCategory;
import com.planifai.core.dto.FinanceCategoryStatisticItem;
import com.planifai.core.dto.FinanceCategoryStatisticsResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.FinancialHealthStatus;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.dto.MonthlyObligationsSummaryResponse;
import com.planifai.core.dto.RecurringExpenseRequest;
import com.planifai.core.dto.RecurringExpenseResponse;
import com.planifai.core.dto.SavingsGoalRequest;
import com.planifai.core.dto.SavingsGoalResponse;
import com.planifai.core.dto.SavingsGoalSummaryResponse;
import com.planifai.core.dto.UpcomingPaymentItem;
import com.planifai.core.finance.domain.FinanceConstants;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.dashboard.ExpenseCategoryBreakdown;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.dashboard.FinanceCategoryStatistic;
import com.planifai.core.finance.domain.model.dashboard.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.domain.model.recurring.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.recurring.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.domain.model.goal.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.goal.SavingsGoalsSummary;
import com.planifai.core.finance.domain.model.goal.SavingsGoalStatus;
import com.planifai.core.finance.domain.model.recurring.UpcomingPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceRestMapper {

    @Mapping(target = "id", ignore = true)
    Expense toDomain(ExpenseRequest request);

    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toExpenseResponse(List<Expense> expenses);

    @Mapping(target = "id", ignore = true)
    Income toDomain(IncomeRequest request);

    IncomeResponse toResponse(Income income);

    List<IncomeResponse> toIncomeResponse(List<Income> incomes);

    default RecurringExpense toDomain(RecurringExpenseRequest request) {
        if (request == null) {
            return null;
        }

        return RecurringExpense.builder()
                .name(request.getName())
                .amount(toBigDecimal(request.getAmount()))
                .category(toDomain(request.getCategory()))
                .recurrence(toDomain(request.getRecurrence()))
                .paymentDay(request.getPaymentDay())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive())
                .notes(request.getNotes())
                .build();
    }

    default RecurringExpenseResponse toResponse(RecurringExpense recurringExpense) {
        return new RecurringExpenseResponse()
                .id(recurringExpense.getId())
                .name(recurringExpense.getName())
                .amount(toDouble(recurringExpense.getAmount()))
                .category(toResponse(recurringExpense.getCategory()))
                .recurrence(toResponse(recurringExpense.getRecurrence()))
                .paymentDay(recurringExpense.getPaymentDay())
                .startDate(recurringExpense.getStartDate())
                .endDate(recurringExpense.getEndDate())
                .active(recurringExpense.getActive())
                .notes(recurringExpense.getNotes());
    }

    List<RecurringExpenseResponse> toRecurringExpenseResponse(List<RecurringExpense> recurringExpenses);

    default Budget toDomain(BudgetRequest request) {
        if (request == null) {
            return null;
        }

        return Budget.builder()
                .month(toYearMonth(request.getMonth()))
                .category(toDomain(request.getCategory()))
                .limitAmount(toBigDecimal(request.getLimitAmount()))
                .active(request.getActive())
                .notes(request.getNotes())
                .build();
    }

    default BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse()
                .id(budget.getId())
                .month(budget.getMonth() != null ? budget.getMonth().toString() : null)
                .category(toResponse(budget.getCategory()))
                .limitAmount(toDouble(budget.getLimitAmount()))
                .active(budget.getActive())
                .notes(budget.getNotes())
                .consumedAmount(0.0)
                .remainingAmount(toDouble(budget.getLimitAmount()))
                .overspentAmount(0.0)
                .consumptionPercentage(0.0)
                .status(com.planifai.core.dto.BudgetStatus.OK)
                .alerts(Collections.emptyList());
    }

    List<BudgetResponse> toBudgetResponse(List<Budget> budgets);

    default SavingsGoal toDomain(SavingsGoalRequest request) {
        if (request == null) {
            return null;
        }

        return SavingsGoal.builder()
                .name(request.getName())
                .targetAmount(toBigDecimal(request.getTargetAmount()))
                .currentAmount(toBigDecimal(request.getCurrentAmount()))
                .targetDate(request.getTargetDate())
                .category(toDomain(request.getCategory()))
                .status(toDomain(request.getStatus()))
                .monthlySavingRate(toBigDecimal(request.getMonthlySavingRate()))
                .notes(request.getNotes())
                .build();
    }

    default SavingsGoalResponse toResponse(SavingsGoal savingsGoal) {
        return new SavingsGoalResponse()
                .id(savingsGoal.getId())
                .name(savingsGoal.getName())
                .targetAmount(toDouble(savingsGoal.getTargetAmount()))
                .currentAmount(toDouble(savingsGoal.getCurrentAmount()))
                .targetDate(savingsGoal.getTargetDate())
                .category(toResponse(savingsGoal.getCategory()))
                .status(toResponse(savingsGoal.getStatus()))
                .monthlySavingRate(toDouble(savingsGoal.getMonthlySavingRate()))
                .notes(savingsGoal.getNotes())
                .remainingAmount(toDouble(savingsGoal.remainingAmount()))
                .progressPercentage(toDouble(savingsGoal.progressPercentage()))
                .estimatedMonthsToCompletion(savingsGoal.estimatedMonthsToCompletion())
                .estimatedCompletionDate(savingsGoal.estimatedCompletionDate(LocalDate.now()))
                .createdAt(savingsGoal.getCreatedAt());
    }

    List<SavingsGoalResponse> toSavingsGoalResponse(List<SavingsGoal> savingsGoals);

    default SavingsGoalSummaryResponse toResponse(SavingsGoalsSummary summary) {
        return new SavingsGoalSummaryResponse()
                .totalGoals(summary.totalGoals())
                .activeGoals(summary.activeGoals())
                .completedGoals(summary.completedGoals())
                .pausedGoals(summary.pausedGoals())
                .cancelledGoals(summary.cancelledGoals())
                .totalTargetAmount(toDouble(summary.totalTargetAmount()))
                .totalCurrentAmount(toDouble(summary.totalCurrentAmount()))
                .totalRemainingAmount(toDouble(summary.totalRemainingAmount()))
                .overallProgressPercentage(toDouble(summary.overallProgressPercentage()))
                .monthlySavingRate(toDouble(summary.monthlySavingRate()))
                .estimatedMonthsToCompletion(summary.estimatedMonthsToCompletion())
                .estimatedCompletionDate(summary.estimatedCompletionDate())
                .nearestGoalToComplete(summary.nearestGoalToComplete() != null
                        ? toResponse(summary.nearestGoalToComplete())
                        : null);
    }

    default MonthlyObligationsSummaryResponse toResponse(MonthlyObligationsSummary summary) {
        return new MonthlyObligationsSummaryResponse()
                .month(summary.month().toString())
                .totalRecurringObligations(toDouble(summary.totalRecurringObligations()))
                .pendingObligations(toDouble(summary.pendingObligations()))
                .paidOrRegisteredObligations(toDouble(summary.paidOrRegisteredObligations()))
                .realAvailableMoney(toDouble(summary.realAvailableMoney()))
                .upcomingPayments(summary.upcomingPayments().stream()
                        .map(this::toResponse)
                        .toList());
    }

    default UpcomingPaymentItem toResponse(UpcomingPayment upcomingPayment) {
        return new UpcomingPaymentItem()
                .recurringExpenseId(upcomingPayment.recurringExpenseId())
                .name(upcomingPayment.name())
                .amount(toDouble(upcomingPayment.amount()))
                .category(toResponse(upcomingPayment.category()))
                .dueDate(upcomingPayment.dueDate())
                .paymentDay(upcomingPayment.paymentDay())
                .status(com.planifai.core.dto.ObligationPaymentStatus.valueOf(upcomingPayment.status().name()));
    }

    default FinanceDashboardResponse toResponse(FinanceDashboard dashboard) {
        return new FinanceDashboardResponse()
                .month(dashboard.month().toString())
                .totalIncome(toDouble(dashboard.totalIncome()))
                .totalExpenses(toDouble(dashboard.totalExpenses()))
                .netBalance(toDouble(dashboard.netBalance()))
                .savingsAmount(toDouble(dashboard.savingsAmount()))
                .savingsRate(toDouble(dashboard.savingsRate()))
                .healthStatus(FinancialHealthStatus.valueOf(dashboard.healthStatus().name()))
                .expensesByCategory(dashboard.expensesByCategory().stream()
                        .map(this::toResponse)
                        .toList());
    }

    default FinanceCategoryStatisticsResponse toResponse(FinanceCategoryStatistics statistics) {
        return new FinanceCategoryStatisticsResponse()
                .month(statistics.month().toString())
                .totalExpenses(toDouble(statistics.totalExpenses()))
                .categories(statistics.categories().stream()
                        .map(this::toResponse)
                        .toList());
    }

    default FinanceCategoryStatisticItem toResponse(FinanceCategoryStatistic statistic) {
        return new FinanceCategoryStatisticItem()
                .category(toResponse(statistic.category()))
                .amount(toDouble(statistic.amount()))
                .percentage(toDouble(statistic.percentage()));
    }

    default com.planifai.core.dto.ExpenseCategoryBreakdown toResponse(
            ExpenseCategoryBreakdown breakdown
    ) {
        return new com.planifai.core.dto.ExpenseCategoryBreakdown()
                .category(FinanceCategory.valueOf(breakdown.category().name()))
                .totalAmount(toDouble(breakdown.totalAmount()))
                .percentage(toDouble(breakdown.percentage()));
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private YearMonth toYearMonth(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(FinanceConstants.INVALID_MONTH, exception);
        }
    }

    default ExpenseCategory toDomain(
            FinanceCategory category
    ) {
        return category != null
                ? ExpenseCategory.valueOf(category.name())
                : null;
    }

    private FinanceCategory toResponse(
            ExpenseCategory category
    ) {
        return category != null
                ? FinanceCategory.valueOf(category.name())
                : null;
    }

    private RecurringExpenseRecurrence toDomain(com.planifai.core.dto.Recurrence recurrence) {
        if (recurrence == null) {
            return null;
        }
        if (recurrence == com.planifai.core.dto.Recurrence.ONE_OFF) {
            throw new IllegalArgumentException(FinanceConstants.RECURRING_EXPENSE_RECURRENCE_UNSUPPORTED);
        }
        return RecurringExpenseRecurrence.valueOf(recurrence.name());
    }

    private com.planifai.core.dto.Recurrence toResponse(RecurringExpenseRecurrence recurrence) {
        return recurrence != null
                ? com.planifai.core.dto.Recurrence.valueOf(recurrence.name())
                : null;
    }

    private SavingsGoalCategory toDomain(
            com.planifai.core.dto.SavingsGoalCategory category
    ) {
        return category != null
                ? SavingsGoalCategory.valueOf(category.name())
                : null;
    }

    private com.planifai.core.dto.SavingsGoalCategory toResponse(
            SavingsGoalCategory category
    ) {
        return category != null
                ? com.planifai.core.dto.SavingsGoalCategory.valueOf(category.name())
                : null;
    }

    private SavingsGoalStatus toDomain(
            com.planifai.core.dto.SavingsGoalStatus status
    ) {
        return status != null
                ? SavingsGoalStatus.valueOf(status.name())
                : null;
    }

    private com.planifai.core.dto.SavingsGoalStatus toResponse(
            SavingsGoalStatus status
    ) {
        return status != null
                ? com.planifai.core.dto.SavingsGoalStatus.valueOf(status.name())
                : null;
    }

}
