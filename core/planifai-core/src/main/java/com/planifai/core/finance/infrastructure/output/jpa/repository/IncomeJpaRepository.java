package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.infrastructure.output.jpa.entity.IncomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncomeJpaRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByIncomeDateBetween(LocalDate from, LocalDate to);
}
