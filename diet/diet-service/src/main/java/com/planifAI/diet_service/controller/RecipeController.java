package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/recipes-db")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeDBService;

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> findAll() {
        return ResponseEntity.ok(recipeDBService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeDBService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> create(@RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeDBService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> update(@PathVariable UUID id, @RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeDBService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeDBService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
