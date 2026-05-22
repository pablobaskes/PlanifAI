package com.planifai.core.finance.infrastructure.output.jpa.repository;

import com.planifai.core.finance.domain.model.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;
import com.planifai.core.finance.infrastructure.output.jpa.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsGoalJpaRepository extends JpaRepository<SavingsGoalEntity, Long> {

    List<SavingsGoalEntity> findByCategory(SavingsGoalCategory category);

    List<SavingsGoalEntity> findByStatus(SavingsGoalStatus status);
}
