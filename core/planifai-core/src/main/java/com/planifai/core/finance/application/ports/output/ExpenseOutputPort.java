package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.Expense;

import java.util.List;

public interface ExpenseOutputPort {

    List<Expense> findAll();

    Expense save(Expense expense);
}
