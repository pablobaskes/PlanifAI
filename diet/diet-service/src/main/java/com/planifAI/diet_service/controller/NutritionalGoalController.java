package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.NutritionalGoalDTO;
import com.planifAI.diet_service.service.NutritionalGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/goals")
@RequiredArgsConstructor
public class NutritionalGoalController {

    private final NutritionalGoalService goalService;

    @GetMapping
    public ResponseEntity<List<NutritionalGoalDTO>> findAll() {
        return ResponseEntity.ok(goalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutritionalGoalDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NutritionalGoalDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(goalService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<NutritionalGoalDTO> create(@RequestBody NutritionalGoalDTO dto) {
        return ResponseEntity.ok(goalService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NutritionalGoalDTO> update(@PathVariable UUID id, @RequestBody NutritionalGoalDTO dto) {
        return ResponseEntity.ok(goalService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
