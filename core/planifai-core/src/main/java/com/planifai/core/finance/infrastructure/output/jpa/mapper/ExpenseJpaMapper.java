package com.planifai.core.finance.infrastructure.output.jpa.mapper;

import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.infrastructure.output.jpa.entity.ExpenseEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseJpaMapper {

    Expense toDomain(ExpenseEntity entity);

    ExpenseEntity toEntity(Expense expense);

    List<Expense> toDomain(List<ExpenseEntity> entities);
}
