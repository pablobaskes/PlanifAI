package com.planifai.core.finance.application.ports.input;

import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.RecurringExpense;

import java.time.YearMonth;
import java.util.List;

public interface FinanceInputPort {

    List<Expense> getExpenses();

    Expense createExpense(Expense expense);

    List<Income> getIncomes();

    Income createIncome(Income income);

    FinanceDashboard getDashboard(YearMonth month);

    MonthlyObligationsSummary getMonthlyObligationsSummary(YearMonth month);

    List<RecurringExpense> getRecurringExpenses();

    RecurringExpense createRecurringExpense(RecurringExpense recurringExpense);

    RecurringExpense updateRecurringExpense(Long id, RecurringExpense recurringExpense);

    void deleteRecurringExpense(Long id);
}
