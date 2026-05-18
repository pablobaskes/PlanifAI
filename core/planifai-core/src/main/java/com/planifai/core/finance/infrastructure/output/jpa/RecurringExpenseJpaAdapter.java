package com.planifai.core.finance.infrastructure.output.jpa;

import com.planifai.core.finance.application.ports.output.RecurringExpenseOutputPort;
import com.planifai.core.finance.domain.model.ExpenseCategory;
import com.planifai.core.finance.domain.model.RecurringExpense;
import com.planifai.core.finance.infrastructure.output.jpa.entity.RecurringExpenseEntity;
import com.planifai.core.finance.infrastructure.output.jpa.mapper.RecurringExpenseJpaMapper;
import com.planifai.core.finance.infrastructure.output.jpa.repository.RecurringExpenseJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class RecurringExpenseJpaAdapter implements RecurringExpenseOutputPort {

    private final RecurringExpenseJpaRepository recurringExpenseJpaRepository;
    private final RecurringExpenseJpaMapper recurringExpenseJpaMapper;

    public RecurringExpenseJpaAdapter(
            RecurringExpenseJpaRepository recurringExpenseJpaRepository,
            RecurringExpenseJpaMapper recurringExpenseJpaMapper
    ) {
        this.recurringExpenseJpaRepository = recurringExpenseJpaRepository;
        this.recurringExpenseJpaMapper = recurringExpenseJpaMapper;
    }

    @Override
    public List<RecurringExpense> findAll() {
        return recurringExpenseJpaMapper.toDomain(recurringExpenseJpaRepository.findAll());
    }

    @Override
    public List<RecurringExpense> findByCategory(ExpenseCategory category) {
        return recurringExpenseJpaMapper.toDomain(recurringExpenseJpaRepository.findByCategory(category));
    }

    @Override
    public List<RecurringExpense> findByActive(boolean active) {
        return recurringExpenseJpaMapper.toDomain(recurringExpenseJpaRepository.findByActive(active));
    }

    @Override
    public List<RecurringExpense> findActiveWithinPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return recurringExpenseJpaMapper.toDomain(
                recurringExpenseJpaRepository.findActiveWithinPeriod(periodStart, periodEnd)
        );
    }

    @Override
    public Optional<RecurringExpense> findById(Long id) {
        return recurringExpenseJpaRepository.findById(id)
                .map(recurringExpenseJpaMapper::toDomain);
    }

    @Override
    public RecurringExpense save(RecurringExpense recurringExpense) {
        RecurringExpenseEntity savedEntity = recurringExpenseJpaRepository.save(recurringExpenseJpaMapper.toEntity(recurringExpense));
        return recurringExpenseJpaMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        recurringExpenseJpaRepository.deleteById(id);
    }
}
