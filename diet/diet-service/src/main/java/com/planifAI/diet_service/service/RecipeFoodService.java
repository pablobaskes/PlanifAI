package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.RecipeFoodDTO;
import com.planifAI.diet_service.mapper.RecipeFoodMapper;
import com.planifAI.diet_service.model.RecipeFood;
import com.planifAI.diet_service.repository.RecipeFoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeFoodService {

    private final RecipeFoodRepository recipeFoodRepository;
    private final RecipeFoodMapper recipeFoodMapper;

    public List<RecipeFoodDTO> findAll() {
        return recipeFoodMapper.toDtoList(recipeFoodRepository.findAll());
    }

    public RecipeFoodDTO findById(UUID id) {
        return recipeFoodRepository.findById(id)
                .map(recipeFoodMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Recipe food link not found"));
    }

    public RecipeFoodDTO create(RecipeFoodDTO dto) {
        RecipeFood entity = recipeFoodMapper.toEntity(dto);
        return recipeFoodMapper.toDto(recipeFoodRepository.save(entity));
    }

    public RecipeFoodDTO update(UUID id, RecipeFoodDTO dto) {
        RecipeFood entity = recipeFoodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe food link not found"));
        entity.setQuantityGramsMl(dto.getQuantityGramsMl());
        entity.setNotes(dto.getNotes());
        return recipeFoodMapper.toDto(recipeFoodRepository.save(entity));
    }

    public void delete(UUID id) {
        recipeFoodRepository.deleteById(id);
    }
}
