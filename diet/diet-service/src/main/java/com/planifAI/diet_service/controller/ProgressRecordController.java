package com.planifAI.diet_service.controller;

import com.planifAI.diet_service.dto.ProgressRecordDTO;
import com.planifAI.diet_service.service.ProgressRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diet/progress")
@RequiredArgsConstructor
public class ProgressRecordController {

    private final ProgressRecordService recordService;

    @GetMapping
    public ResponseEntity<List<ProgressRecordDTO>> findAll() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressRecordDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recordService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressRecordDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(recordService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ProgressRecordDTO> create(@RequestBody ProgressRecordDTO dto) {
        return ResponseEntity.ok(recordService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgressRecordDTO> update(@PathVariable UUID id, @RequestBody ProgressRecordDTO dto) {
        return ResponseEntity.ok(recordService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
