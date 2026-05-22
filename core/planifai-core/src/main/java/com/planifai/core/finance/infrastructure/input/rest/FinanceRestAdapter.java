package com.planifai.core.finance.infrastructure.input.rest;

import com.planifai.core.api.FinanceApi;
import com.planifai.core.dto.BudgetRequest;
import com.planifai.core.dto.BudgetResponse;
import com.planifai.core.dto.BudgetSummaryResponse;
import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceCategory;
import com.planifai.core.dto.FinanceCategoryResponse;
import com.planifai.core.dto.FinanceCategoryStatisticsResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.dto.MonthlyObligationsSummaryResponse;
import com.planifai.core.dto.RecurringExpenseRequest;
import com.planifai.core.dto.RecurringExpenseResponse;
import com.planifai.core.dto.SavingsGoalRequest;
import com.planifai.core.dto.SavingsGoalResponse;
import com.planifai.core.dto.SavingsGoalSummaryResponse;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.domain.FinanceConstants;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.infrastructure.input.rest.mapper.FinanceRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class FinanceRestAdapter implements FinanceApi {

    private final FinanceInputPort financeInputPort;
    private final FinanceRestMapper financeRestMapper;

    @Override
    public ResponseEntity<List<ExpenseResponse>> getExpenses(FinanceCategory category) {
        return ResponseEntity.ok(financeRestMapper.toExpenseResponse(
                financeInputPort.getExpenses(financeRestMapper.toDomain(category))
        ));
    }

    @Override
    public ResponseEntity<List<ExpenseResponse>> getFinanceTransactions(FinanceCategory category) {
        return ResponseEntity.ok(financeRestMapper.toExpenseResponse(
                financeInputPort.getFinanceTransactions(financeRestMapper.toDomain(category))
        ));
    }

    @Override
    public ResponseEntity<ExpenseResponse> createExpense(ExpenseRequest expenseRequest) {
        Expense createdExpense = financeInputPort.createExpense(financeRestMapper.toDomain(expenseRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdExpense));
    }

    @Override
    public ResponseEntity<List<IncomeResponse>> getIncomes() {
        return ResponseEntity.ok(financeRestMapper.toIncomeResponse(financeInputPort.getIncomes()));
    }

    @Override
    public ResponseEntity<IncomeResponse> createIncome(IncomeRequest incomeRequest) {
        Income createdIncome = financeInputPort.createIncome(financeRestMapper.toDomain(incomeRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdIncome));
    }

    @Override
    public ResponseEntity<FinanceDashboardResponse> getFinanceDashboard(String month) {
        FinanceDashboard dashboard = financeInputPort.getDashboard(parseMonth(month));
        return ResponseEntity.ok(financeRestMapper.toResponse(dashboard));
    }

    @Override
    public ResponseEntity<List<FinanceCategoryResponse>> getFinanceCategories() {
        return ResponseEntity.ok(Arrays.stream(FinanceCategory.values())
                .map(category -> new FinanceCategoryResponse()
                        .code(category)
                        .label(toCategoryLabel(category)))
                .toList());
    }

    @Override
    public ResponseEntity<FinanceCategoryStatisticsResponse> getFinanceCategoryStatistics(String month) {
        return ResponseEntity.ok(financeRestMapper.toResponse(
                financeInputPort.getCategoryStatistics(parseMonth(month))
        ));
    }

    @Override
    public ResponseEntity<List<SavingsGoalResponse>> getSavingsGoals() {
        return ResponseEntity.ok(financeRestMapper.toSavingsGoalResponse(financeInputPort.getSavingsGoals()));
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> createSavingsGoal(SavingsGoalRequest savingsGoalRequest) {
        SavingsGoal createdSavingsGoal = financeInputPort.createSavingsGoal(
                financeRestMapper.toDomain(savingsGoalRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdSavingsGoal));
    }

    @Override
    public ResponseEntity<SavingsGoalSummaryResponse> getSavingsGoalsSummary() {
        return ResponseEntity.ok(financeRestMapper.toResponse(financeInputPort.getSavingsGoalsSummary()));
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> getSavingsGoalById(Long id) {
        return ResponseEntity.ok(financeRestMapper.toResponse(financeInputPort.getSavingsGoalById(id)));
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> updateSavingsGoal(Long id, SavingsGoalRequest savingsGoalRequest) {
        SavingsGoal updatedSavingsGoal = financeInputPort.updateSavingsGoal(
                id,
                financeRestMapper.toDomain(savingsGoalRequest)
        );
        return ResponseEntity.ok(financeRestMapper.toResponse(updatedSavingsGoal));
    }

    @Override
    public ResponseEntity<Void> deleteSavingsGoal(Long id) {
        financeInputPort.deleteSavingsGoal(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RecurringExpenseResponse>> getRecurringExpenses(FinanceCategory category) {
        return ResponseEntity.ok(financeRestMapper.toRecurringExpenseResponse(
                financeInputPort.getRecurringExpenses(financeRestMapper.toDomain(category))
        ));
    }

    @Override
    public ResponseEntity<RecurringExpenseResponse> createRecurringExpense(RecurringExpenseRequest recurringExpenseRequest) {
        RecurringExpense createdRecurringExpense = financeInputPort.createRecurringExpense(
                financeRestMapper.toDomain(recurringExpenseRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdRecurringExpense));
    }

    @Override
    public ResponseEntity<RecurringExpenseResponse> updateRecurringExpense(Long id, RecurringExpenseRequest recurringExpenseRequest) {
        RecurringExpense updatedRecurringExpense = financeInputPort.updateRecurringExpense(
                id,
                financeRestMapper.toDomain(recurringExpenseRequest)
        );
        return ResponseEntity.ok(financeRestMapper.toResponse(updatedRecurringExpense));
    }

    @Override
    public ResponseEntity<Void> deleteRecurringExpense(Long id) {
        financeInputPort.deleteRecurringExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MonthlyObligationsSummaryResponse> getMonthlyObligationsSummary(String month) {
        return ResponseEntity.ok(financeRestMapper.toResponse(
                financeInputPort.getMonthlyObligationsSummary(parseMonth(month))
        ));
    }

    @Override
    public ResponseEntity<List<BudgetResponse>> getFinanceBudgets(String month) {
        return ResponseEntity.ok(financeRestMapper.toBudgetResponse(
                financeInputPort.getBudgets(parseMonth(month))
        ));
    }

    @Override
    public ResponseEntity<BudgetResponse> getFinanceBudgetById(Long id) {
        return ResponseEntity.ok(financeRestMapper.toResponse(financeInputPort.getBudgetById(id)));
    }

    @Override
    public ResponseEntity<BudgetResponse> createFinanceBudget(BudgetRequest budgetRequest) {
        Budget createdBudget = financeInputPort.createBudget(financeRestMapper.toDomain(budgetRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdBudget));
    }

    @Override
    public ResponseEntity<BudgetResponse> updateFinanceBudget(Long id, BudgetRequest budgetRequest) {
        Budget updatedBudget = financeInputPort.updateBudget(id, financeRestMapper.toDomain(budgetRequest));
        return ResponseEntity.ok(financeRestMapper.toResponse(updatedBudget));
    }

    @Override
    public ResponseEntity<Void> deleteFinanceBudget(Long id) {
        financeInputPort.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BudgetSummaryResponse> getFinanceBudgetSummary(String month) {
        parseMonth(month);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    private String toCategoryLabel(FinanceCategory category) {
        String value = category.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException(FinanceConstants.INVALID_MONTH);
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(FinanceConstants.INVALID_MONTH, exception);
        }
    }
}
