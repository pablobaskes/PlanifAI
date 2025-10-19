package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.MealPlanDTO;
import com.planifAI.diet_service.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    public ResponseEntity<List<MealPlanDTO>> findAll() {
        return ResponseEntity.ok(mealPlanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealPlanDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(mealPlanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MealPlanDTO> create(@RequestBody MealPlanDTO dto) {
        return ResponseEntity.ok(mealPlanService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealPlanDTO> update(@PathVariable UUID id, @RequestBody MealPlanDTO dto) {
        return ResponseEntity.ok(mealPlanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mealPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

