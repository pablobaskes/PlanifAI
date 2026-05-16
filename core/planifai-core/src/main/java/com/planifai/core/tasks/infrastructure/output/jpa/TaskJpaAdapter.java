package com.planifai.core.tasks.infrastructure.output.jpa;

import com.planifai.core.tasks.application.ports.output.TaskOutputPort;
import com.planifai.core.tasks.domain.model.Task;
import com.planifai.core.tasks.infrastructure.output.jpa.entity.TaskEntity;
import com.planifai.core.tasks.infrastructure.output.jpa.mapper.TaskJpaMapper;
import com.planifai.core.tasks.infrastructure.output.jpa.repository.TaskJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskJpaAdapter implements TaskOutputPort {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskJpaMapper taskJpaMapper;

    public TaskJpaAdapter(TaskJpaRepository taskJpaRepository, TaskJpaMapper taskJpaMapper) {
        this.taskJpaRepository = taskJpaRepository;
        this.taskJpaMapper = taskJpaMapper;
    }

    @Override
    public List<Task> findAll() {
        return taskJpaMapper.toDomain(taskJpaRepository.findAll());
    }

    @Override
    public Task save(Task task) {
        TaskEntity savedEntity = taskJpaRepository.save(taskJpaMapper.toEntity(task));
        return taskJpaMapper.toDomain(savedEntity);
    }
}
