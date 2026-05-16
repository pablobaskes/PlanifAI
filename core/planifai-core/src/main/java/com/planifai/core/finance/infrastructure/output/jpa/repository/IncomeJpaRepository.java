package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.infrastructure.output.jpa.entity.IncomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeJpaRepository extends JpaRepository<IncomeEntity, Long> {
}
