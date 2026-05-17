package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseOutputPort {

    List<Expense> findAll();

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    Expense save(Expense expense);
}
