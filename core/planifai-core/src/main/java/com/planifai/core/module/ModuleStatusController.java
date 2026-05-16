package com.planifai.core.module;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ModuleStatusController {

    @GetMapping("/api/tasks/module")
    public Map<String, Object> tasksModule() {
        return Map.of("module", "tasks", "status", "planned", "boundedContext", "planifai-core");
    }

    @GetMapping("/api/finance/module")
    public Map<String, Object> financeModule() {
        return Map.of("module", "finance", "status", "planned", "boundedContext", "planifai-core");
    }

    @GetMapping("/api/core/modules")
    public Map<String, Object> modules() {
        return Map.of("service", "planifai-core", "modules", List.of("tasks", "finance"));
    }
}
