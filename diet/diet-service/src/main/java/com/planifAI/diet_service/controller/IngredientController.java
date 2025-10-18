package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.IngredientDto;
import com.planifAI.diet_service.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<IngredientDto> createIngredient(
            @RequestHeader("X-Auth-Roles") String roles,
            @RequestBody IngredientDto dto) {

        if (!roles.contains("ROLE_USER")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(ingredientService.createIngredient(dto));
    }

    @GetMapping
    public ResponseEntity<List<IngredientDto>> getAllIngredients(@RequestHeader("X-Auth-Roles") String roles) {

        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }
}
