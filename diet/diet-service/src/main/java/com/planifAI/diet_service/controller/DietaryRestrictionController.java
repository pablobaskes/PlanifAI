package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.DietaryRestrictionDTO;
import com.planifAI.diet_service.service.DietaryRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/dietary-restrictions")
@RequiredArgsConstructor
public class DietaryRestrictionController {

    private final DietaryRestrictionService restrictionService;

    @GetMapping
    public ResponseEntity<List<DietaryRestrictionDTO>> findAll() {
        return ResponseEntity.ok(restrictionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DietaryRestrictionDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(restrictionService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DietaryRestrictionDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(restrictionService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<DietaryRestrictionDTO> create(@RequestBody DietaryRestrictionDTO dto) {
        return ResponseEntity.ok(restrictionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietaryRestrictionDTO> update(@PathVariable UUID id, @RequestBody DietaryRestrictionDTO dto) {
        return ResponseEntity.ok(restrictionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        restrictionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
