package com.planifai.core.finance.infrastructure.input.rest.mapper;

import com.planifai.core.dto.ExpenseRequest;
import com.planifai.core.dto.ExpenseResponse;
import com.planifai.core.dto.IncomeRequest;
import com.planifai.core.dto.IncomeResponse;
import com.planifai.core.finance.domain.model.Expense;
import com.planifai.core.finance.domain.model.Income;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceRestMapper {

    @Mapping(target = "id", ignore = true)
    Expense toDomain(ExpenseRequest request);

    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toExpenseResponse(List<Expense> expenses);

    @Mapping(target = "id", ignore = true)
    Income toDomain(IncomeRequest request);

    IncomeResponse toResponse(Income income);

    List<IncomeResponse> toIncomeResponse(List<Income> incomes);
}
