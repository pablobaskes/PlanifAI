package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.PantryItemDTO;
import com.planifAI.diet_service.mapper.PantryItemMapper;
import com.planifAI.diet_service.model.Ingredient;
import com.planifAI.diet_service.model.PantryItem;
import com.planifAI.diet_service.repository.PantryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PantryItemService {

    private final PantryItemRepository pantryRepository;
    private final PantryItemMapper pantryMapper;

    public PantryItemDTO findById(UUID id) {
        return pantryRepository.findById(id)
                .map(pantryMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
    }

    public List<PantryItemDTO> findAll() {
        return pantryMapper.toDtoList(pantryRepository.findAll());
    }

    public PantryItemDTO create(PantryItemDTO dto) {
        PantryItem item = pantryMapper.toEntity(dto);
        return pantryMapper.toDto(pantryRepository.save(item));
    }

    public PantryItemDTO update(UUID id, PantryItemDTO dto) {
        PantryItem entity = pantryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        pantryMapper.updateEntityFromDto(dto, entity);
        return pantryMapper.toDto(pantryRepository.save(entity));
    }

    public void delete(UUID id) {
        pantryRepository.deleteById(id);
    }
}
