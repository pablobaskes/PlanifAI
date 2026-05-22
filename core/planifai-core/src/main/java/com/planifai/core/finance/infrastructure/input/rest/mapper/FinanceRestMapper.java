package com.planifai.core.finance.infrastructure.input.rest.mapper;

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
import com.planifai.core.dto.UpcomingPaymentItem;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.FinanceCategoryStatistic;
import com.planifai.core.finance.domain.model.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.domain.model.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.SavingsGoal;
import com.planifai.core.finance.domain.model.UpcomingPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setName(request.getName());
        recurringExpense.setAmount(toBigDecimal(request.getAmount()));
        recurringExpense.setCategory(toDomain(request.getCategory()));
        recurringExpense.setRecurrence(toDomain(request.getRecurrence()));
        recurringExpense.setPaymentDay(request.getPaymentDay());
        recurringExpense.setStartDate(request.getStartDate());
        recurringExpense.setEndDate(request.getEndDate());
        recurringExpense.setActive(request.getActive());
        recurringExpense.setNotes(request.getNotes());
        return recurringExpense;
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

    default SavingsGoal toDomain(SavingsGoalRequest request) {
        if (request == null) {
            return null;
        }

        SavingsGoal savingsGoal = new SavingsGoal();
        savingsGoal.setName(request.getName());
        savingsGoal.setTargetAmount(toBigDecimal(request.getTargetAmount()));
        savingsGoal.setCurrentAmount(toBigDecimal(request.getCurrentAmount()));
        savingsGoal.setTargetDate(request.getTargetDate());
        savingsGoal.setCategory(toDomain(request.getCategory()));
        savingsGoal.setStatus(toDomain(request.getStatus()));
        savingsGoal.setMonthlySavingRate(toBigDecimal(request.getMonthlySavingRate()));
        savingsGoal.setNotes(request.getNotes());
        return savingsGoal;
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
                .progressPercentage(toDouble(calculateSavingsGoalProgress(savingsGoal)))
                .estimatedMonthsToCompletion(calculateEstimatedMonthsToCompletion(savingsGoal))
                .estimatedCompletionDate(calculateEstimatedCompletionDate(savingsGoal))
                .createdAt(savingsGoal.getCreatedAt());
    }

    List<SavingsGoalResponse> toSavingsGoalResponse(List<SavingsGoal> savingsGoals);

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
            com.planifai.core.finance.domain.model.ExpenseCategoryBreakdown breakdown
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

    default com.planifai.core.finance.domain.model.ExpenseCategory toDomain(
            FinanceCategory category
    ) {
        return category != null
                ? com.planifai.core.finance.domain.model.ExpenseCategory.valueOf(category.name())
                : null;
    }

    private FinanceCategory toResponse(
            com.planifai.core.finance.domain.model.ExpenseCategory category
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
            throw new IllegalArgumentException("Recurring expense recurrence must be MONTHLY or YEARLY.");
        }
        return RecurringExpenseRecurrence.valueOf(recurrence.name());
    }

    private com.planifai.core.dto.Recurrence toResponse(RecurringExpenseRecurrence recurrence) {
        return recurrence != null
                ? com.planifai.core.dto.Recurrence.valueOf(recurrence.name())
                : null;
    }

    private com.planifai.core.finance.domain.model.SavingsGoalCategory toDomain(
            com.planifai.core.dto.SavingsGoalCategory category
    ) {
        return category != null
                ? com.planifai.core.finance.domain.model.SavingsGoalCategory.valueOf(category.name())
                : null;
    }

    private com.planifai.core.dto.SavingsGoalCategory toResponse(
            com.planifai.core.finance.domain.model.SavingsGoalCategory category
    ) {
        return category != null
                ? com.planifai.core.dto.SavingsGoalCategory.valueOf(category.name())
                : null;
    }

    private com.planifai.core.finance.domain.model.SavingsGoalStatus toDomain(
            com.planifai.core.dto.SavingsGoalStatus status
    ) {
        return status != null
                ? com.planifai.core.finance.domain.model.SavingsGoalStatus.valueOf(status.name())
                : null;
    }

    private com.planifai.core.dto.SavingsGoalStatus toResponse(
            com.planifai.core.finance.domain.model.SavingsGoalStatus status
    ) {
        return status != null
                ? com.planifai.core.dto.SavingsGoalStatus.valueOf(status.name())
                : null;
    }

    private BigDecimal calculateSavingsGoalProgress(SavingsGoal savingsGoal) {
        if (savingsGoal.getTargetAmount() == null
                || savingsGoal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0
                || savingsGoal.getCurrentAmount() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal progress = savingsGoal.getCurrentAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(savingsGoal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        return progress.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(100) : progress;
    }

    private Integer calculateEstimatedMonthsToCompletion(SavingsGoal savingsGoal) {
        BigDecimal remainingAmount = savingsGoal.remainingAmount();
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        if (savingsGoal.getMonthlySavingRate() == null
                || savingsGoal.getMonthlySavingRate().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return remainingAmount
                .divide(savingsGoal.getMonthlySavingRate(), 0, RoundingMode.CEILING)
                .intValue();
    }

    private LocalDate calculateEstimatedCompletionDate(SavingsGoal savingsGoal) {
        Integer monthsToCompletion = calculateEstimatedMonthsToCompletion(savingsGoal);
        return monthsToCompletion != null ? LocalDate.now().plusMonths(monthsToCompletion) : null;
    }
}
