package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.ShoppingListItemDTO;
import com.planifAI.diet_service.service.ShoppingListItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/shopping-list-items")
@RequiredArgsConstructor
public class ShoppingListItemController {

    private final ShoppingListItemService itemService;

    @GetMapping
    public ResponseEntity<List<ShoppingListItemDTO>> findAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListItemDTO> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ShoppingListItemDTO> create(@RequestBody ShoppingListItemDTO dto) {
        return ResponseEntity.ok(itemService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListItemDTO> update(@PathVariable("id") UUID id, @RequestBody ShoppingListItemDTO dto) {
        return ResponseEntity.ok(itemService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

