package com.planifai.core.finance.infrastructure.input.rest.mapper;

import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceCategory;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.FinancialHealthStatus;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.dto.MonthlyObligationsSummaryResponse;
import com.planifai.core.dto.RecurringExpenseRequest;
import com.planifai.core.dto.RecurringExpenseResponse;
import com.planifai.core.dto.UpcomingPaymentItem;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.domain.model.RecurringExpenseRecurrence;
import com.planifai.core.finance.domain.model.UpcomingPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
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

    private com.planifai.core.finance.domain.model.ExpenseCategory toDomain(
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
}
