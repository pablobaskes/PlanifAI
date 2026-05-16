package com.planifai.core.tasks.application.usecase;

import com.planifai.core.tasks.application.ports.input.TaskInputPort;
import com.planifai.core.tasks.application.ports.output.TaskOutputPort;
import com.planifai.core.tasks.domain.model.Task;
import com.planifai.core.tasks.domain.model.TaskPriority;
import com.planifai.core.tasks.domain.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TaskUseCase implements TaskInputPort {

    private final TaskOutputPort taskOutputPort;

    public TaskUseCase(TaskOutputPort taskOutputPort) {
        this.taskOutputPort = taskOutputPort;
    }

    @Override
    public List<Task> getTasks() {
        return taskOutputPort.findAll();
    }

    @Override
    public Task createTask(Task task) {
        validate(task);
        task.setId(null);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(OffsetDateTime.now());
        }
        return taskOutputPort.save(task);
    }

    private void validate(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task is required.");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task title is required.");
        }
    }
}
