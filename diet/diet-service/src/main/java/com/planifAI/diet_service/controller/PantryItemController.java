package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.PantryItemDTO;
import com.planifAI.diet_service.service.PantryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/pantry")
@RequiredArgsConstructor
public class PantryItemController {

    private final PantryItemService pantryItemService;

    @GetMapping
    public ResponseEntity<List<PantryItemDTO>> findAll() {
        return ResponseEntity.ok(pantryItemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PantryItemDTO> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(pantryItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PantryItemDTO> create(@RequestBody PantryItemDTO dto) {
        return ResponseEntity.ok(pantryItemService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PantryItemDTO> update(@PathVariable("id") UUID id, @RequestBody PantryItemDTO dto) {
        return ResponseEntity.ok(pantryItemService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        pantryItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
