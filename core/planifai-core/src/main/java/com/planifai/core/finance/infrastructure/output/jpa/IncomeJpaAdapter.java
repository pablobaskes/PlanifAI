package com.planifai.core.finance.infrastructure.output.jpa;

import com.planifai.core.finance.application.ports.output.IncomeOutputPort;
import com.planifai.core.finance.domain.model.Income;
import com.planifai.core.finance.infrastructure.output.jpa.entity.IncomeEntity;
import com.planifai.core.finance.infrastructure.output.jpa.mapper.IncomeJpaMapper;
import com.planifai.core.finance.infrastructure.output.jpa.repository.IncomeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IncomeJpaAdapter implements IncomeOutputPort {

    private final IncomeJpaRepository incomeJpaRepository;
    private final IncomeJpaMapper incomeJpaMapper;

    public IncomeJpaAdapter(IncomeJpaRepository incomeJpaRepository, IncomeJpaMapper incomeJpaMapper) {
        this.incomeJpaRepository = incomeJpaRepository;
        this.incomeJpaMapper = incomeJpaMapper;
    }

    @Override
    public List<Income> findAll() {
        return incomeJpaMapper.toDomain(incomeJpaRepository.findAll());
    }

    @Override
    public Income save(Income income) {
        IncomeEntity savedEntity = incomeJpaRepository.save(incomeJpaMapper.toEntity(income));
        return incomeJpaMapper.toDomain(savedEntity);
    }
}
