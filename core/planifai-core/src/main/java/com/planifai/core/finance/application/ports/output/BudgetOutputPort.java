package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetOutputPort {

    List<Budget> findAll();

    List<Budget> findByMonth(YearMonth month);

    List<Budget> findByMonthAndActive(YearMonth month, boolean active);

    List<Budget> findByCategory(ExpenseCategory category);

    Optional<Budget> findById(Long id);

    boolean existsActiveByMonthAndCategoryExcludingId(YearMonth month, ExpenseCategory category, Long excludedId);

    Budget save(Budget budget);

    void deleteById(Long id);
}
