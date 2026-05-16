package com.planifai.core.tasks.infrastructure.output.jpa.mapper;

import com.planifai.core.tasks.domain.model.Task;
import com.planifai.core.tasks.infrastructure.output.jpa.entity.TaskEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskJpaMapper {

    Task toDomain(TaskEntity entity);

    TaskEntity toEntity(Task task);

    List<Task> toDomain(List<TaskEntity> entities);
}
