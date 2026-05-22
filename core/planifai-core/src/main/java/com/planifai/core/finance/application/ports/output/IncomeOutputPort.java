package com.planifai.core.finance.application.ports.output;

import com.planifai.core.finance.domain.model.transaction.Income;

import java.time.LocalDate;
import java.util.List;

public interface IncomeOutputPort {

    List<Income> findAll();

    List<Income> findByIncomeDateBetween(LocalDate from, LocalDate to);

    Income save(Income income);
}
