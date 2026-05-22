package com.planifai.core.finance.infrastructure.output.jpa.mapper;

import com.planifai.core.finance.domain.model.goal.SavingsGoal;
import com.planifai.core.finance.infrastructure.output.jpa.entity.SavingsGoalEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavingsGoalJpaMapper {

    SavingsGoal toDomain(SavingsGoalEntity entity);

    SavingsGoalEntity toEntity(SavingsGoal savingsGoal);

    List<SavingsGoal> toDomain(List<SavingsGoalEntity> entities);
}
