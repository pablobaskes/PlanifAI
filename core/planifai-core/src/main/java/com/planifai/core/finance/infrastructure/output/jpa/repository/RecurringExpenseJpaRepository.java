package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.infrastructure.output.jpa.entity.RecurringExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseJpaRepository extends JpaRepository<RecurringExpenseEntity, Long> {

    List<RecurringExpenseEntity> findByActive(boolean active);

    List<RecurringExpenseEntity> findByCategory(ExpenseCategory category);

    @Query("""
            select recurringExpense
            from RecurringExpenseEntity recurringExpense
            where recurringExpense.active = true
              and recurringExpense.startDate <= :periodEnd
              and (recurringExpense.endDate is null or recurringExpense.endDate >= :periodStart)
            """)
    List<RecurringExpenseEntity> findActiveWithinPeriod(
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
