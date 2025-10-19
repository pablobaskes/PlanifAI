package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> findAll() {
        return ResponseEntity.ok(recipeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> create(@RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> update(@PathVariable UUID id, @RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
