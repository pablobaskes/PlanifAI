package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.RecipeV2DTO;
import com.planifAI.diet_service.service.RecipeV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/recipes-db")
@RequiredArgsConstructor
public class RecipeV2Controller {

    private final RecipeV2Service recipeDBService;

    @GetMapping
    public ResponseEntity<List<RecipeV2DTO>> findAll() {
        return ResponseEntity.ok(recipeDBService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeV2DTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeDBService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeV2DTO> create(@RequestBody RecipeV2DTO dto) {
        return ResponseEntity.ok(recipeDBService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeV2DTO> update(@PathVariable UUID id, @RequestBody RecipeV2DTO dto) {
        return ResponseEntity.ok(recipeDBService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeDBService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
