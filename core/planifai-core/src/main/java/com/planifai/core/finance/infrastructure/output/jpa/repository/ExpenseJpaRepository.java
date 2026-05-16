package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.infrastructure.output.jpa.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, Long> {
}
