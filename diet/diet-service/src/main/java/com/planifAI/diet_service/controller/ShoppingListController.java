package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.ShoppingListDTO;
import com.planifAI.diet_service.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public ResponseEntity<List<ShoppingListDTO>> findAll() {
        return ResponseEntity.ok(shoppingListService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListDTO> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(shoppingListService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ShoppingListDTO> create(@RequestBody ShoppingListDTO dto) {
        return ResponseEntity.ok(shoppingListService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListDTO> update(@PathVariable("id") UUID id, @RequestBody ShoppingListDTO dto) {
        return ResponseEntity.ok(shoppingListService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        shoppingListService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
