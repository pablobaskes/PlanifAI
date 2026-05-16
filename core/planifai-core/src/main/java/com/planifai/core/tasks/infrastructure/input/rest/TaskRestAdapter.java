package com.planifai.core.tasks.infrastructure.input.rest;

import com.planifai.core.api.TasksApi;
import com.planifai.core.dto.TaskRequest;
import com.planifai.core.dto.TaskResponse;
import com.planifai.core.tasks.application.ports.input.TaskInputPort;
import com.planifai.core.tasks.domain.model.Task;
import com.planifai.core.tasks.infrastructure.input.rest.mapper.TaskRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TaskRestAdapter implements TasksApi {

    private final TaskInputPort taskInputPort;
    private final TaskRestMapper taskRestMapper;

    public TaskRestAdapter(TaskInputPort taskInputPort, TaskRestMapper taskRestMapper) {
        this.taskInputPort = taskInputPort;
        this.taskRestMapper = taskRestMapper;
    }

    @Override
    public ResponseEntity<List<TaskResponse>> getTasks() {
        return ResponseEntity.ok(taskRestMapper.toResponse(taskInputPort.getTasks()));
    }

    @Override
    public ResponseEntity<TaskResponse> createTask(TaskRequest taskRequest) {
        Task createdTask = taskInputPort.createTask(taskRestMapper.toDomain(taskRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRestMapper.toResponse(createdTask));
    }
}
