package com.planifai.core.tasks.application.ports.output;

import com.planifai.core.tasks.domain.model.Task;

import java.util.List;

public interface TaskOutputPort {

    List<Task> findAll();

    Task save(Task task);
}
