package com.planifai.core.finance.infrastructure.input.rest;

import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.FinancialHealthStatus;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.infrastructure.input.rest.mapper.FinanceRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceRestAdapterTest {

    @Test
    void getFinanceDashboardParsesMonthAndReturnsGeneratedResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<FinanceDashboardResponse> response = adapter.getFinanceDashboard("2026-05");

        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedMonth);
        assertEquals("2026-05", response.getBody().getMonth());
        assertEquals(1000.0, response.getBody().getTotalIncome());
        assertEquals(900.0, response.getBody().getTotalExpenses());
        assertEquals(100.0, response.getBody().getNetBalance());
        assertEquals(10.0, response.getBody().getSavingsRate());
        assertEquals(FinancialHealthStatus.WARNING, response.getBody().getHealthStatus());
    }

    private static final class FakeFinanceInputPort implements FinanceInputPort {

        private YearMonth requestedMonth;

        @Override
        public List<Expense> getExpenses() {
            return List.of();
        }

        @Override
        public Expense createExpense(Expense expense) {
            return expense;
        }

        @Override
        public List<Income> getIncomes() {
            return List.of();
        }

        @Override
        public Income createIncome(Income income) {
            return income;
        }

        @Override
        public FinanceDashboard getDashboard(YearMonth month) {
            this.requestedMonth = month;
            return new FinanceDashboard(
                    month,
                    new BigDecimal("1000.00"),
                    new BigDecimal("900.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("10.00"),
                    FinanceHealthStatus.WARNING,
                    List.of()
            );
        }
    }

    private static final class TestFinanceRestMapper implements FinanceRestMapper {

        @Override
        public Expense toDomain(ExpenseRequest request) {
            return null;
        }

        @Override
        public ExpenseResponse toResponse(Expense expense) {
            return null;
        }

        @Override
        public List<ExpenseResponse> toExpenseResponse(List<Expense> expenses) {
            return List.of();
        }

        @Override
        public Income toDomain(IncomeRequest request) {
            return null;
        }

        @Override
        public IncomeResponse toResponse(Income income) {
            return null;
        }

        @Override
        public List<IncomeResponse> toIncomeResponse(List<Income> incomes) {
            return List.of();
        }
    }
}
