package com.planifai.core.finance.infrastructure.output.jpa.mapper;

import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.infrastructure.output.jpa.entity.IncomeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeJpaMapper {

    Income toDomain(IncomeEntity entity);

    IncomeEntity toEntity(Income income);

    List<Income> toDomain(List<IncomeEntity> entities);
}
