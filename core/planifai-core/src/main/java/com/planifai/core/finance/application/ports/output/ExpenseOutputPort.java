package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.ExpenseCategory;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseOutputPort {

    List<Expense> findAll();

    List<Expense> findByCategory(ExpenseCategory category);

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    Expense save(Expense expense);
}
