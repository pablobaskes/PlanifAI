package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.FoodDTO;
import com.planifAI.diet_service.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodDTO>> findAll() {
        return ResponseEntity.ok(foodService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(foodService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FoodDTO> create(@RequestBody FoodDTO dto) {
        return ResponseEntity.ok(foodService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodDTO> update(@PathVariable UUID id, @RequestBody FoodDTO dto) {
        return ResponseEntity.ok(foodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
