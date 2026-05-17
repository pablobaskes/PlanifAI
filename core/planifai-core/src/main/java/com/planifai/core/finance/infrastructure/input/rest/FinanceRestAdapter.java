package com.planifai.core.finance.infrastructure.input.rest;

import com.planifai.core.api.FinanceApi;
import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.infrastructure.input.rest.mapper.FinanceRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
public class FinanceRestAdapter implements FinanceApi {

    private final FinanceInputPort financeInputPort;
    private final FinanceRestMapper financeRestMapper;

    public FinanceRestAdapter(FinanceInputPort financeInputPort, FinanceRestMapper financeRestMapper) {
        this.financeInputPort = financeInputPort;
        this.financeRestMapper = financeRestMapper;
    }

    @Override
    public ResponseEntity<List<ExpenseResponse>> getExpenses() {
        return ResponseEntity.ok(financeRestMapper.toExpenseResponse(financeInputPort.getExpenses()));
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
        FinanceDashboard dashboard = financeInputPort.getDashboard(YearMonth.parse(month));
        return ResponseEntity.ok(financeRestMapper.toResponse(dashboard));
    }
}
