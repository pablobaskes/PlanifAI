package com.planifai.core.finance.infrastructure.output.jpa;

import com.planifai.core.finance.application.ports.output.SavingsGoalOutputPort;
import com.planifai.core.finance.domain.model.SavingsGoal;
import com.planifai.core.finance.domain.model.SavingsGoalCategory;
import com.planifai.core.finance.domain.model.SavingsGoalStatus;
import com.planifai.core.finance.infrastructure.output.jpa.entity.SavingsGoalEntity;
import com.planifai.core.finance.infrastructure.output.jpa.mapper.SavingsGoalJpaMapper;
import com.planifai.core.finance.infrastructure.output.jpa.repository.SavingsGoalJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SavingsGoalJpaAdapter implements SavingsGoalOutputPort {

    private final SavingsGoalJpaRepository savingsGoalJpaRepository;
    private final SavingsGoalJpaMapper savingsGoalJpaMapper;

    public SavingsGoalJpaAdapter(
            SavingsGoalJpaRepository savingsGoalJpaRepository,
            SavingsGoalJpaMapper savingsGoalJpaMapper
    ) {
        this.savingsGoalJpaRepository = savingsGoalJpaRepository;
        this.savingsGoalJpaMapper = savingsGoalJpaMapper;
    }

    @Override
    public List<SavingsGoal> findAll() {
        return savingsGoalJpaMapper.toDomain(savingsGoalJpaRepository.findAll());
    }

    @Override
    public List<SavingsGoal> findByCategory(SavingsGoalCategory category) {
        return savingsGoalJpaMapper.toDomain(savingsGoalJpaRepository.findByCategory(category));
    }

    @Override
    public List<SavingsGoal> findByStatus(SavingsGoalStatus status) {
        return savingsGoalJpaMapper.toDomain(savingsGoalJpaRepository.findByStatus(status));
    }

    @Override
    public Optional<SavingsGoal> findById(Long id) {
        return savingsGoalJpaRepository.findById(id)
                .map(savingsGoalJpaMapper::toDomain);
    }

    @Override
    public SavingsGoal save(SavingsGoal savingsGoal) {
        savingsGoal.validate();
        SavingsGoalEntity savedEntity = savingsGoalJpaRepository.save(savingsGoalJpaMapper.toEntity(savingsGoal));
        return savingsGoalJpaMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        savingsGoalJpaRepository.deleteById(id);
    }
}
