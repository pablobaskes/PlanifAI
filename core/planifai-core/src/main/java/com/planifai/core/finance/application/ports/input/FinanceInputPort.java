package com.planifai.core.finance.application.ports.input;

import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.Income;

import java.util.List;

public interface FinanceInputPort {

    List<Expense> getExpenses();

    Expense createExpense(Expense expense);

    List<Income> getIncomes();

    Income createIncome(Income income);
}
