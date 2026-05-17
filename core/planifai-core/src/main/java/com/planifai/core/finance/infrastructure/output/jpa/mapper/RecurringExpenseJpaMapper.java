package com.planifai.core.finance.infrastructure.output.jpa.mapper;

import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.infrastructure.output.jpa.entity.RecurringExpenseEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecurringExpenseJpaMapper {

    RecurringExpense toDomain(RecurringExpenseEntity entity);

    RecurringExpenseEntity toEntity(RecurringExpense recurringExpense);

    List<RecurringExpense> toDomain(List<RecurringExpenseEntity> entities);
}
