package com.planifai.core.tasks.infrastructure.output.jpa.repository;

import com.planifai.core.tasks.infrastructure.output.jpa.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {
}
