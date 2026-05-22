package com.planifai.core.finance.application.ports.input;

import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.FinanceCategoryStatistics;
import com.planifai.core.finance.domain.model.FinanceDashboard;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.domain.model.MonthlyObligationsSummary;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.domain.model.SavingsGoal;

import java.time.YearMonth;
import java.util.List;

public interface FinanceInputPort {

    List<Expense> getExpenses();

    List<Expense> getExpenses(ExpenseCategory category);

    List<Expense> getFinanceTransactions(ExpenseCategory category);

    Expense createExpense(Expense expense);

    List<Income> getIncomes();

    Income createIncome(Income income);

    FinanceDashboard getDashboard(YearMonth month);

    FinanceCategoryStatistics getCategoryStatistics(YearMonth month);

    MonthlyObligationsSummary getMonthlyObligationsSummary(YearMonth month);

    List<RecurringExpense> getRecurringExpenses();

    List<RecurringExpense> getRecurringExpenses(ExpenseCategory category);

    RecurringExpense createRecurringExpense(RecurringExpense recurringExpense);

    RecurringExpense updateRecurringExpense(Long id, RecurringExpense recurringExpense);

    void deleteRecurringExpense(Long id);

    List<SavingsGoal> getSavingsGoals();

    SavingsGoal getSavingsGoalById(Long id);

    SavingsGoal createSavingsGoal(SavingsGoal savingsGoal);

    SavingsGoal updateSavingsGoal(Long id, SavingsGoal savingsGoal);

    void deleteSavingsGoal(Long id);
}
