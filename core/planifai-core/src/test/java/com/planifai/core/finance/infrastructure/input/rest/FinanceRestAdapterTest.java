package com.planifai.core.finance.infrastructure.input.rest;

import com.planifai.core.dto.BudgetRequest;
import com.planifai.core.dto.BudgetResponse;
import com.planifai.core.dto.BudgetSummaryResponse;
import com.planifai.core.dto.CashflowResponse;
import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.FinanceCategory;
import com.planifai.core.dto.FinanceCategoryResponse;
import com.planifai.core.dto.FinanceCategoryStatisticsResponse;
import com.planifai.core.dto.FinanceDashboardResponse;
import com.planifai.core.dto.FinancialHealthStatus;
import com.planifai.core.dto.FinancialTimelineResponse;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.dto.MonthlyObligationsSummaryResponse;
import com.planifai.core.dto.RecurringExpenseRequest;
import com.planifai.core.dto.RecurringExpenseResponse;
import com.planifai.core.dto.Recurrence;
import com.planifai.core.dto.SavingsGoalCategory;
import com.planifai.core.dto.SavingsGoalRequest;
import com.planifai.core.dto.SavingsGoalResponse;
import com.planifai.core.dto.SavingsGoalStatus;
import com.planifai.core.dto.SavingsGoalSummaryResponse;
import com.planifai.core.finance.application.ports.input.FinanceInputPort;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.budget.BudgetCategoryStatus;
import com.planifai.core.finance.domain.model.budget.BudgetStatus;
import com.planifai.core.finance.domain.model.budget.BudgetSummary;
import com.planifai.core.finance.domain.model.cashflow.Cashflow;
import com.planifai.core.finance.domain.model.cashflow.CashflowMonth;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.dashboard.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.dashboard.FinanceDashboard;
import com.planifai.core.finance.domain.model.dashboard.FinanceHealthStatus;
import com.planifai.core.finance.domain.model.transaction.Income;
import com.planifai.core.finance.domain.model.recurring.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;
import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.domain.model.goal.SavingsGoalsSummary;
import com.planifai.core.finance.domain.model.timeline.FinancialTimeline;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEvent;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventStatus;
import com.planifai.core.finance.domain.model.timeline.FinancialTimelineEventType;
import com.planifai.core.finance.infrastructure.input.rest.mapper.FinanceRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void createRecurringExpenseReturnsCreatedResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());
        RecurringExpenseRequest request = new RecurringExpenseRequest(
                "Rent",
                1000.0,
                FinanceCategory.HOUSING,
                Recurrence.MONTHLY,
                10,
                LocalDate.of(2026, 1, 1),
                true
        );

        ResponseEntity<RecurringExpenseResponse> response = adapter.createRecurringExpense(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Rent", response.getBody().getName());
    }

    @Test
    void getFinanceCategoriesReturnsBackendControlledValues() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<List<FinanceCategoryResponse>> response = adapter.getFinanceCategories();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(FinanceCategory.FOOD, response.getBody().get(0).getCode());
    }

    @Test
    void getFinanceTransactionsPassesCategoryFilter() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<List<ExpenseResponse>> response = adapter.getFinanceTransactions(FinanceCategory.FOOD);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ExpenseCategory.FOOD, financeInputPort.requestedCategory);
    }

    @Test
    void getRecurringExpensesPassesCategoryFilter() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<List<RecurringExpenseResponse>> response = adapter.getRecurringExpenses(FinanceCategory.SUBSCRIPTIONS);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ExpenseCategory.SUBSCRIPTIONS, financeInputPort.requestedCategory);
    }

    @Test
    void createRecurringExpensePropagatesInvalidRecurrenceForGlobalHandler() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());
        RecurringExpenseRequest request = new RecurringExpenseRequest(
                "Invalid",
                1000.0,
                FinanceCategory.OTHER,
                Recurrence.ONE_OFF,
                10,
                LocalDate.of(2026, 1, 1),
                true
        );

        assertThrows(IllegalArgumentException.class, () -> adapter.createRecurringExpense(request));
    }

    @Test
    void getMonthlyObligationsSummaryParsesMonthAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<MonthlyObligationsSummaryResponse> response = adapter.getMonthlyObligationsSummary("2026-05");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedMonth);
        assertEquals("2026-05", response.getBody().getMonth());
    }

    @Test
    void getMonthlyObligationsSummaryPropagatesInvalidMonthForGlobalHandler() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        assertThrows(IllegalArgumentException.class, () -> adapter.getMonthlyObligationsSummary("2026-13"));
    }

    @Test
    void getFinanceCategoryStatisticsParsesMonthAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<FinanceCategoryStatisticsResponse> response = adapter.getFinanceCategoryStatistics("2026-05");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedMonth);
        assertEquals("2026-05", response.getBody().getMonth());
    }

    @Test
    void getFinanceTimelinePassesDateRangeAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<FinancialTimelineResponse> response = adapter.getFinanceTimeline(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(LocalDate.of(2026, 5, 1), financeInputPort.requestedFromDate);
        assertEquals(LocalDate.of(2026, 5, 31), financeInputPort.requestedToDate);
        assertEquals(LocalDate.of(2026, 5, 1), response.getBody().getFrom());
        assertEquals(LocalDate.of(2026, 5, 31), response.getBody().getTo());
        assertEquals(com.planifai.core.dto.FinancialTimelineEventType.INCOME, response.getBody().getEvents().get(0).getType());
        assertEquals(false, response.getBody().getEvents().get(0).getProjected());
        assertEquals(com.planifai.core.dto.FinancialTimelineEventStatus.POSTED, response.getBody().getEvents().get(0).getStatus());
    }

    @Test
    void getFinanceCashflowParsesMonthRangeAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<CashflowResponse> response = adapter.getFinanceCashflow("2026-05", "2026-06");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedFromMonth);
        assertEquals(YearMonth.of(2026, 6), financeInputPort.requestedToMonth);
        assertEquals("2026-05", response.getBody().getFrom());
        assertEquals("2026-06", response.getBody().getTo());
        assertEquals("2026-05", response.getBody().getMonths().get(0).getMonth());
        assertEquals(1000.0, response.getBody().getMonths().get(0).getExpectedIncome());
        assertEquals(600.0, response.getBody().getMonths().get(0).getExpectedExpenses());
    }

    @Test
    void getFinanceCashflowPropagatesInvalidMonthForGlobalHandler() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        assertThrows(IllegalArgumentException.class, () -> adapter.getFinanceCashflow("2026-13", "2026-06"));
    }

    @Test
    void getFinanceBudgetsParsesMonthAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<List<BudgetResponse>> response = adapter.getFinanceBudgets("2026-05");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedMonth);
        assertEquals(1, response.getBody().size());
        assertEquals("2026-05", response.getBody().get(0).getMonth());
        assertEquals(FinanceCategory.FOOD, response.getBody().get(0).getCategory());
    }

    @Test
    void createFinanceBudgetReturnsCreatedResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());
        BudgetRequest request = new BudgetRequest("2026-05", FinanceCategory.FOOD, 400.0, true);

        ResponseEntity<BudgetResponse> response = adapter.createFinanceBudget(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
        assertEquals("2026-05", response.getBody().getMonth());
        assertEquals(400.0, response.getBody().getLimitAmount());
    }

    @Test
    void getFinanceBudgetSummaryParsesMonthAndReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<BudgetSummaryResponse> response = adapter.getFinanceBudgetSummary("2026-05");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(YearMonth.of(2026, 5), financeInputPort.requestedMonth);
        assertEquals("2026-05", response.getBody().getMonth());
        assertEquals(400.0, response.getBody().getTotalLimitAmount());
        assertEquals(320.0, response.getBody().getTotalConsumedAmount());
        assertEquals(com.planifai.core.dto.BudgetStatus.WARNING, response.getBody().getStatus());
    }

    @Test
    void createSavingsGoalReturnsCreatedResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());
        SavingsGoalRequest request = new SavingsGoalRequest(
                "Travel",
                2000.0,
                500.0,
                SavingsGoalCategory.TRAVEL,
                SavingsGoalStatus.ACTIVE
        );

        ResponseEntity<SavingsGoalResponse> response = adapter.createSavingsGoal(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Travel", response.getBody().getName());
        assertEquals(25.0, response.getBody().getProgressPercentage());
    }

    @Test
    void createSavingsGoalPropagatesFunctionalValidationErrorForGlobalHandler() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        financeInputPort.rejectSavingsGoalCreate = true;
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());
        SavingsGoalRequest request = new SavingsGoalRequest(
                "Invalid",
                0.0,
                0.0,
                SavingsGoalCategory.OTHER,
                SavingsGoalStatus.ACTIVE
        );

        assertThrows(IllegalArgumentException.class, () -> adapter.createSavingsGoal(request));
    }

    @Test
    void deleteSavingsGoalReturnsNoContent() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<Void> response = adapter.deleteSavingsGoal(1L);

        assertEquals(204, response.getStatusCode().value());
        assertEquals(1L, financeInputPort.deletedSavingsGoalId);
    }

    @Test
    void getSavingsGoalsSummaryReturnsResponse() {
        FakeFinanceInputPort financeInputPort = new FakeFinanceInputPort();
        FinanceRestAdapter adapter = new FinanceRestAdapter(financeInputPort, new TestFinanceRestMapper());

        ResponseEntity<SavingsGoalSummaryResponse> response = adapter.getSavingsGoalsSummary();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getTotalGoals());
        assertEquals(1, response.getBody().getActiveGoals());
        assertEquals("Travel", response.getBody().getNearestGoalToComplete().getName());
    }

    private static final class FakeFinanceInputPort implements FinanceInputPort {

        private YearMonth requestedMonth;
        private LocalDate requestedFromDate;
        private LocalDate requestedToDate;
        private YearMonth requestedFromMonth;
        private YearMonth requestedToMonth;
        private ExpenseCategory requestedCategory;
        private boolean rejectSavingsGoalCreate;
        private Long deletedSavingsGoalId;

        @Override
        public List<Expense> getExpenses() {
            return List.of();
        }

        @Override
        public List<Expense> getExpenses(ExpenseCategory category) {
            this.requestedCategory = category;
            return List.of();
        }

        @Override
        public List<Expense> getFinanceTransactions(ExpenseCategory category) {
            this.requestedCategory = category;
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
            return FinanceDashboard.builder()
                    .month(month)
                    .totalIncome(new BigDecimal("1000.00"))
                    .totalExpenses(new BigDecimal("900.00"))
                    .netBalance(new BigDecimal("100.00"))
                    .savingsAmount(new BigDecimal("100.00"))
                    .savingsRate(new BigDecimal("10.00"))
                    .healthStatus(FinanceHealthStatus.WARNING)
                    .expensesByCategory(List.of())
                    .build();
        }

        @Override
        public FinancialTimeline getFinancialTimeline(LocalDate from, LocalDate to) {
            this.requestedFromDate = from;
            this.requestedToDate = to;
            return FinancialTimeline.builder()
                    .from(from)
                    .to(to)
                    .events(List.of(FinancialTimelineEvent.builder()
                            .id("income-1")
                            .date(from)
                            .type(FinancialTimelineEventType.INCOME)
                            .label("Salary")
                            .amount(new BigDecimal("1000.00"))
                            .category(null)
                            .source("income")
                            .projected(false)
                            .status(FinancialTimelineEventStatus.POSTED)
                            .build()))
                    .build();
        }

        @Override
        public Cashflow getCashflow(YearMonth from, YearMonth to) {
            this.requestedFromMonth = from;
            this.requestedToMonth = to;
            return Cashflow.builder()
                    .from(from)
                    .to(to)
                    .months(List.of(CashflowMonth.builder()
                            .month(from)
                            .expectedIncome(new BigDecimal("1000.00"))
                            .expectedExpenses(new BigDecimal("600.00"))
                            .projectedBalance(new BigDecimal("400.00"))
                            .netCashflow(new BigDecimal("400.00"))
                            .savingsAmount(new BigDecimal("400.00"))
                            .savingsRate(new BigDecimal("40.00"))
                            .build()))
                    .build();
        }

        @Override
        public FinanceCategoryStatistics getCategoryStatistics(YearMonth month) {
            this.requestedMonth = month;
            return FinanceCategoryStatistics.builder()
                    .month(month)
                    .totalExpenses(BigDecimal.ZERO)
                    .categories(List.of())
                    .build();
        }

        @Override
        public MonthlyObligationsSummary getMonthlyObligationsSummary(YearMonth month) {
            this.requestedMonth = month;
            return MonthlyObligationsSummary.builder()
                    .month(month)
                    .totalRecurringObligations(BigDecimal.ZERO)
                    .pendingObligations(BigDecimal.ZERO)
                    .paidOrRegisteredObligations(BigDecimal.ZERO)
                    .realAvailableMoney(BigDecimal.ZERO)
                    .upcomingPayments(List.of())
                    .build();
        }

        @Override
        public List<RecurringExpense> getRecurringExpenses() {
            return List.of();
        }

        @Override
        public List<RecurringExpense> getRecurringExpenses(ExpenseCategory category) {
            this.requestedCategory = category;
            return List.of();
        }

        @Override
        public RecurringExpense createRecurringExpense(RecurringExpense recurringExpense) {
            recurringExpense.setId(1L);
            return recurringExpense;
        }

        @Override
        public RecurringExpense updateRecurringExpense(Long id, RecurringExpense recurringExpense) {
            recurringExpense.setId(id);
            return recurringExpense;
        }

        @Override
        public void deleteRecurringExpense(Long id) {
        }

        @Override
        public List<Budget> getBudgets(YearMonth month) {
            this.requestedMonth = month;
            return List.of(budget(month));
        }

        @Override
        public BudgetSummary getBudgetSummary(YearMonth month) {
            this.requestedMonth = month;
            return BudgetSummary.builder()
                    .month(month)
                    .totalLimitAmount(new BigDecimal("400.00"))
                    .totalConsumedAmount(new BigDecimal("320.00"))
                    .totalRemainingAmount(new BigDecimal("80.00"))
                    .totalOverspentAmount(BigDecimal.ZERO)
                    .overallConsumptionPercentage(new BigDecimal("80.00"))
                    .status(BudgetStatus.WARNING)
                    .categories(List.of(BudgetCategoryStatus.builder()
                            .budgetId(1L)
                            .category(ExpenseCategory.FOOD)
                            .limitAmount(new BigDecimal("400.00"))
                            .consumedAmount(new BigDecimal("320.00"))
                            .remainingAmount(new BigDecimal("80.00"))
                            .overspentAmount(BigDecimal.ZERO)
                            .consumptionPercentage(new BigDecimal("80.00"))
                            .status(BudgetStatus.WARNING)
                            .alerts(List.of())
                            .build()))
                    .alerts(List.of())
                    .build();
        }

        @Override
        public Budget getBudgetById(Long id) {
            Budget budget = budget(YearMonth.of(2026, 5));
            budget.setId(id);
            return budget;
        }

        @Override
        public Budget createBudget(Budget budget) {
            budget.setId(1L);
            return budget;
        }

        @Override
        public Budget updateBudget(Long id, Budget budget) {
            budget.setId(id);
            return budget;
        }

        @Override
        public void deleteBudget(Long id) {
        }

        @Override
        public List<SavingsGoal> getSavingsGoals() {
            return List.of(savingsGoal());
        }

        @Override
        public SavingsGoalsSummary getSavingsGoalsSummary() {
            return SavingsGoalsSummary.builder()
                    .totalGoals(2)
                    .activeGoals(1)
                    .completedGoals(1)
                    .pausedGoals(0)
                    .cancelledGoals(0)
                    .totalTargetAmount(new BigDecimal("3000.00"))
                    .totalCurrentAmount(new BigDecimal("1500.00"))
                    .totalRemainingAmount(new BigDecimal("1500.00"))
                    .overallProgressPercentage(new BigDecimal("50.00"))
                    .monthlySavingRate(new BigDecimal("250.00"))
                    .estimatedMonthsToCompletion(6)
                    .estimatedCompletionDate(LocalDate.of(2026, 11, 22))
                    .nearestGoalToComplete(savingsGoal())
                    .build();
        }

        @Override
        public SavingsGoal getSavingsGoalById(Long id) {
            SavingsGoal savingsGoal = savingsGoal();
            savingsGoal.setId(id);
            return savingsGoal;
        }

        @Override
        public SavingsGoal createSavingsGoal(SavingsGoal savingsGoal) {
            if (rejectSavingsGoalCreate) {
                throw new IllegalArgumentException("Invalid savings goal.");
            }
            savingsGoal.setId(1L);
            return savingsGoal;
        }

        @Override
        public SavingsGoal updateSavingsGoal(Long id, SavingsGoal savingsGoal) {
            savingsGoal.setId(id);
            return savingsGoal;
        }

        @Override
        public void deleteSavingsGoal(Long id) {
            deletedSavingsGoalId = id;
        }

        private SavingsGoal savingsGoal() {
            return SavingsGoal.builder()
                    .id(1L)
                    .name("Travel")
                    .targetAmount(new BigDecimal("2000.00"))
                    .currentAmount(new BigDecimal("500.00"))
                    .category(com.planifai.core.finance.domain.model.goal.SavingsGoalCategory.TRAVEL)
                    .status(com.planifai.core.finance.domain.model.goal.SavingsGoalStatus.ACTIVE)
                    .monthlySavingRate(new BigDecimal("250.00"))
                    .build();
        }

        private Budget budget(YearMonth month) {
            return Budget.builder()
                    .id(1L)
                    .month(month)
                    .category(ExpenseCategory.FOOD)
                    .limitAmount(new BigDecimal("400.00"))
                    .active(true)
                    .build();
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

        @Override
        public List<RecurringExpenseResponse> toRecurringExpenseResponse(List<RecurringExpense> recurringExpenses) {
            return List.of();
        }

        @Override
        public List<BudgetResponse> toBudgetResponse(List<Budget> budgets) {
            return budgets.stream()
                    .map(this::toResponse)
                    .toList();
        }

        @Override
        public List<SavingsGoalResponse> toSavingsGoalResponse(List<SavingsGoal> savingsGoals) {
            return savingsGoals.stream()
                    .map(this::toResponse)
                    .toList();
        }

        @Override
        public MonthlyObligationsSummaryResponse toResponse(MonthlyObligationsSummary summary) {
            return new MonthlyObligationsSummaryResponse()
                    .month(summary.month().toString())
                    .totalRecurringObligations(0.0)
                    .pendingObligations(0.0)
                    .paidOrRegisteredObligations(0.0)
                    .realAvailableMoney(0.0)
                    .upcomingPayments(List.of());
        }

        @Override
        public FinanceCategoryStatisticsResponse toResponse(FinanceCategoryStatistics statistics) {
            return new FinanceCategoryStatisticsResponse()
                    .month(statistics.month().toString())
                    .totalExpenses(0.0)
                    .categories(List.of());
        }
    }
}
