package com.planifai.core.tasks.application.ports.input;

import com.planifai.core.tasks.domain.model.Task;

import java.util.List;

public interface TaskInputPort {

    List<Task> getTasks();

    Task createTask(Task task);
}
