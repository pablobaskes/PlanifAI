package com.planifai.core.finance.infrastructure.output.jpa;

import com.planifai.core.finance.application.ports.output.BudgetOutputPort;
import com.planifai.core.finance.domain.FinanceConstants;
import com.planifai.core.finance.domain.model.budget.Budget;
import com.planifai.core.finance.domain.model.transaction.ExpenseCategory;
import com.planifai.core.finance.infrastructure.output.jpa.entity.BudgetEntity;
import com.planifai.core.finance.infrastructure.output.jpa.mapper.BudgetJpaMapper;
import com.planifai.core.finance.infrastructure.output.jpa.repository.BudgetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetJpaAdapter implements BudgetOutputPort {

    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetJpaMapper budgetJpaMapper;

    @Override
    public List<Budget> findAll() {
        return budgetJpaMapper.toDomain(budgetJpaRepository.findAll());
    }

    @Override
    public List<Budget> findByMonth(YearMonth month) {
        return budgetJpaMapper.toDomain(budgetJpaRepository.findByMonth(toMonthValue(month)));
    }

    @Override
    public List<Budget> findByMonthAndActive(YearMonth month, boolean active) {
        return budgetJpaMapper.toDomain(budgetJpaRepository.findByMonthAndActive(toMonthValue(month), active));
    }

    @Override
    public List<Budget> findByCategory(ExpenseCategory category) {
        return budgetJpaMapper.toDomain(budgetJpaRepository.findByCategory(category));
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return budgetJpaRepository.findById(id)
                .map(budgetJpaMapper::toDomain);
    }

    @Override
    public boolean existsActiveByMonthAndCategoryExcludingId(
            YearMonth month,
            ExpenseCategory category,
            Long excludedId
    ) {
        return budgetJpaRepository.existsActiveByMonthAndCategoryExcludingId(
                toMonthValue(month),
                category,
                excludedId
        );
    }

    @Override
    public Budget save(Budget budget) {
        budget.validate();
        if (budget.getActive() == null) {
            budget.setActive(Boolean.TRUE);
        }
        if (Boolean.TRUE.equals(budget.getActive())
                && existsActiveByMonthAndCategoryExcludingId(
                        budget.getMonth(),
                        budget.getCategory(),
                        budget.getId()
                )) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_ACTIVE_DUPLICATE);
        }

        BudgetEntity savedEntity = budgetJpaRepository.save(budgetJpaMapper.toEntity(budget));
        return budgetJpaMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        budgetJpaRepository.deleteById(id);
    }

    private String toMonthValue(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException(FinanceConstants.BUDGET_MONTH_REQUIRED);
        }
        return month.toString();
    }
}
