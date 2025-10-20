package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.IngredientDTO;
import com.planifAI.diet_service.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> findAll() {
        return ResponseEntity.ok(ingredientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientDTO> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ingredientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<IngredientDTO> create(@RequestBody IngredientDTO dto) {
        return ResponseEntity.ok(ingredientService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> update(@PathVariable("id") UUID id, @RequestBody IngredientDTO dto) {
        return ResponseEntity.ok(ingredientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

