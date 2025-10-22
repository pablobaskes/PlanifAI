package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.RecipeFoodDTO;
import com.planifAI.diet_service.service.RecipeFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/recipe-foods")
@RequiredArgsConstructor
public class RecipeFoodController {

    private final RecipeFoodService recipeFoodService;

    @GetMapping
    public ResponseEntity<List<RecipeFoodDTO>> findAll() {
        return ResponseEntity.ok(recipeFoodService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeFoodDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeFoodService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeFoodDTO> create(@RequestBody RecipeFoodDTO dto) {
        return ResponseEntity.ok(recipeFoodService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeFoodDTO> update(@PathVariable UUID id, @RequestBody RecipeFoodDTO dto) {
        return ResponseEntity.ok(recipeFoodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeFoodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
