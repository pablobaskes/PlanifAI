package com.planifai.core.finance.infrastructure.output.jpa.mapper;

import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.infrastructure.output.jpa.entity.BudgetEntity;
import org.mapstruct.Mapper;

import java.time.YearMonth;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetJpaMapper {

    Budget toDomain(BudgetEntity entity);

    BudgetEntity toEntity(Budget budget);

    List<Budget> toDomain(List<BudgetEntity> entities);

    default String map(YearMonth month) {
        return month != null ? month.toString() : null;
    }

    default YearMonth map(String month) {
        return month != null && !month.isBlank() ? YearMonth.parse(month) : null;
    }
}
