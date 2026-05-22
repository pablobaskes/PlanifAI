package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.domain.model.recurring.RecurringExpense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringExpenseOutputPort {

    List<RecurringExpense> findAll();

    List<RecurringExpense> findByCategory(ExpenseCategory category);

    List<RecurringExpense> findByActive(boolean active);

    List<RecurringExpense> findActiveWithinPeriod(LocalDate periodStart, LocalDate periodEnd);

    Optional<RecurringExpense> findById(Long id);

    RecurringExpense save(RecurringExpense recurringExpense);

    void deleteById(Long id);
}
