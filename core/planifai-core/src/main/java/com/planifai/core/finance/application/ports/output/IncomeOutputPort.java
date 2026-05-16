package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.Income;

import java.util.List;

public interface IncomeOutputPort {

    List<Income> findAll();

    Income save(Income income);
}
