package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.infrastructure.output.jpa.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findByMonth(String month);

    List<BudgetEntity> findByMonthAndActive(String month, Boolean active);

    List<BudgetEntity> findByCategory(ExpenseCategory category);

    @Query("""
            select count(budget) > 0
            from BudgetEntity budget
            where budget.month = :month
              and budget.category = :category
              and budget.active = true
              and (:excludedId is null or budget.id <> :excludedId)
            """)
    boolean existsActiveByMonthAndCategoryExcludingId(
            @Param("month") String month,
            @Param("category") ExpenseCategory category,
            @Param("excludedId") Long excludedId
    );
}
