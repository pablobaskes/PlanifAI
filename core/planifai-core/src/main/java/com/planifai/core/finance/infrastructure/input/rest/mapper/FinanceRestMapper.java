package com.planifai.core.finance.infrastructure.input.rest.mapper;

import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.FinancialHealthStatus;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Income;
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
                .category(com.planifai.core.dto.ExpenseCategory.valueOf(breakdown.category().name()))
                .totalAmount(toDouble(breakdown.totalAmount()))
                .percentage(toDouble(breakdown.percentage()));
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
