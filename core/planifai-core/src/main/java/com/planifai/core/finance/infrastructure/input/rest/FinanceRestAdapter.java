package com.planifai.core.finance.infrastructure.input.rest;

import com.planifai.core.api.FinanceApi;
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
import com.planifai.core.finance.domain.FinanceConstants;
import com.planifai.core.finance.domain.exception.RecurringExpenseNotFoundException;
import com.planifai.core.finance.domain.exception.SavingsGoalNotFoundException;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
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
        try {
            Expense createdExpense = financeInputPort.createExpense(financeRestMapper.toDomain(expenseRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdExpense));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<IncomeResponse>> getIncomes() {
        return ResponseEntity.ok(financeRestMapper.toIncomeResponse(financeInputPort.getIncomes()));
    }

    @Override
    public ResponseEntity<IncomeResponse> createIncome(IncomeRequest incomeRequest) {
        try {
            Income createdIncome = financeInputPort.createIncome(financeRestMapper.toDomain(incomeRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdIncome));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<FinanceDashboardResponse> getFinanceDashboard(String month) {
        try {
            FinanceDashboard dashboard = financeInputPort.getDashboard(parseMonth(month));
            return ResponseEntity.ok(financeRestMapper.toResponse(dashboard));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
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
        try {
            return ResponseEntity.ok(financeRestMapper.toResponse(
                    financeInputPort.getCategoryStatistics(parseMonth(month))
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<SavingsGoalResponse>> getSavingsGoals() {
        return ResponseEntity.ok(financeRestMapper.toSavingsGoalResponse(financeInputPort.getSavingsGoals()));
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> createSavingsGoal(SavingsGoalRequest savingsGoalRequest) {
        try {
            SavingsGoal createdSavingsGoal = financeInputPort.createSavingsGoal(
                    financeRestMapper.toDomain(savingsGoalRequest)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdSavingsGoal));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<SavingsGoalSummaryResponse> getSavingsGoalsSummary() {
        return ResponseEntity.ok(financeRestMapper.toResponse(financeInputPort.getSavingsGoalsSummary()));
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> getSavingsGoalById(Long id) {
        try {
            return ResponseEntity.ok(financeRestMapper.toResponse(financeInputPort.getSavingsGoalById(id)));
        } catch (SavingsGoalNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<SavingsGoalResponse> updateSavingsGoal(Long id, SavingsGoalRequest savingsGoalRequest) {
        try {
            SavingsGoal updatedSavingsGoal = financeInputPort.updateSavingsGoal(
                    id,
                    financeRestMapper.toDomain(savingsGoalRequest)
            );
            return ResponseEntity.ok(financeRestMapper.toResponse(updatedSavingsGoal));
        } catch (SavingsGoalNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteSavingsGoal(Long id) {
        try {
            financeInputPort.deleteSavingsGoal(id);
            return ResponseEntity.noContent().build();
        } catch (SavingsGoalNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<RecurringExpenseResponse>> getRecurringExpenses(FinanceCategory category) {
        return ResponseEntity.ok(financeRestMapper.toRecurringExpenseResponse(
                financeInputPort.getRecurringExpenses(financeRestMapper.toDomain(category))
        ));
    }

    @Override
    public ResponseEntity<RecurringExpenseResponse> createRecurringExpense(RecurringExpenseRequest recurringExpenseRequest) {
        try {
            RecurringExpense createdRecurringExpense = financeInputPort.createRecurringExpense(
                    financeRestMapper.toDomain(recurringExpenseRequest)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(financeRestMapper.toResponse(createdRecurringExpense));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<RecurringExpenseResponse> updateRecurringExpense(Long id, RecurringExpenseRequest recurringExpenseRequest) {
        try {
            RecurringExpense updatedRecurringExpense = financeInputPort.updateRecurringExpense(
                    id,
                    financeRestMapper.toDomain(recurringExpenseRequest)
            );
            return ResponseEntity.ok(financeRestMapper.toResponse(updatedRecurringExpense));
        } catch (RecurringExpenseNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteRecurringExpense(Long id) {
        try {
            financeInputPort.deleteRecurringExpense(id);
            return ResponseEntity.noContent().build();
        } catch (RecurringExpenseNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MonthlyObligationsSummaryResponse> getMonthlyObligationsSummary(String month) {
        try {
            return ResponseEntity.ok(financeRestMapper.toResponse(
                    financeInputPort.getMonthlyObligationsSummary(parseMonth(month))
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String toCategoryLabel(FinanceCategory category) {
        String value = category.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(FinanceConstants.INVALID_MONTH, exception);
        }
    }
}
