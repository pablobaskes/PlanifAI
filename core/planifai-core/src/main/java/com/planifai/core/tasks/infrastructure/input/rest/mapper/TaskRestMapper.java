package com.planifai.core.tasks.infrastructure.input.rest.mapper;

import com.planifai.core.dto.TaskRequest;
import com.planifai.core.dto.TaskResponse;
import com.planifai.core.tasks.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Task toDomain(TaskRequest request);

    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponse(List<Task> tasks);
}
