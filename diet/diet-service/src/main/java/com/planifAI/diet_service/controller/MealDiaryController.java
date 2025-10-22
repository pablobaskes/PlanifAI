package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.MealDiaryDTO;
import com.planifAI.diet_service.service.MealDiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/meal-diary")
@RequiredArgsConstructor
public class MealDiaryController {

    private final MealDiaryService mealDiaryService;

    @GetMapping
    public ResponseEntity<List<MealDiaryDTO>> findAll() {
        return ResponseEntity.ok(mealDiaryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealDiaryDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(mealDiaryService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MealDiaryDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(mealDiaryService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<MealDiaryDTO> create(@RequestBody MealDiaryDTO dto) {
        return ResponseEntity.ok(mealDiaryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealDiaryDTO> update(@PathVariable UUID id, @RequestBody MealDiaryDTO dto) {
        return ResponseEntity.ok(mealDiaryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mealDiaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
