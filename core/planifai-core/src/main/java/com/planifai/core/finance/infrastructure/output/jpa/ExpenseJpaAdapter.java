package com.planifai.core.finance.infrastructure.output.jpa;

import com.planifai.core.finance.application.ports.output.ExpenseOutputPort;
import com.planifai.core.finance.domain.model.transaction.Expense;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.infrastructure.output.jpa.entity.ExpenseEntity;
import com.planifai.core.finance.infrastructure.output.jpa.mapper.ExpenseJpaMapper;
import com.planifai.core.finance.infrastructure.output.jpa.repository.ExpenseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseJpaAdapter implements ExpenseOutputPort {

    private final ExpenseJpaRepository expenseJpaRepository;
    private final ExpenseJpaMapper expenseJpaMapper;

    @Override
    public List<Expense> findAll() {
        return expenseJpaMapper.toDomain(expenseJpaRepository.findAll());
    }

    @Override
    public List<Expense> findByCategory(ExpenseCategory category) {
        return expenseJpaMapper.toDomain(expenseJpaRepository.findByCategory(category));
    }

    @Override
    public List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to) {
        return expenseJpaMapper.toDomain(expenseJpaRepository.findByExpenseDateBetween(from, to));
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseEntity savedEntity = expenseJpaRepository.save(expenseJpaMapper.toEntity(expense));
        return expenseJpaMapper.toDomain(savedEntity);
    }
}
