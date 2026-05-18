package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.infrastructure.output.jpa.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByExpenseDateBetween(LocalDate from, LocalDate to);

    List<ExpenseEntity> findByCategory(ExpenseCategory category);
}
