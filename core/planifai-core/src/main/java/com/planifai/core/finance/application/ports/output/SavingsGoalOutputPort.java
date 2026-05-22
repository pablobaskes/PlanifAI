package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.SavingsGoal;
import com.planifai.core.finance.domain.model.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalOutputPort {

    List<SavingsGoal> findAll();

    List<SavingsGoal> findByCategory(SavingsGoalCategory category);

    List<SavingsGoal> findByStatus(SavingsGoalStatus status);

    Optional<SavingsGoal> findById(Long id);

    SavingsGoal save(SavingsGoal savingsGoal);

    void deleteById(Long id);
}
