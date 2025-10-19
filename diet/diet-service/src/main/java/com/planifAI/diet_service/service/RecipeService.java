package com.planifAI.diet_service.service;


import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.mapper.RecipeMapper;
import com.planifAI.diet_service.model.Recipe;
import com.planifAI.diet_service.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public List<RecipeDTO> findAll() {
        return recipeMapper.toDtoList(recipeRepository.findAll());
    }

    public RecipeDTO findById(UUID id) {
        return recipeRepository.findById(id)
                .map(recipeMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public RecipeDTO create(RecipeDTO dto) {
        Recipe entity = recipeMapper.toEntity(dto);
        return recipeMapper.toDto(recipeRepository.save(entity));
    }

    public RecipeDTO update(UUID id, RecipeDTO dto) {
        Recipe entity = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        recipeMapper.updateEntityFromDto(dto, entity);
        return recipeMapper.toDto(recipeRepository.save(entity));
    }

    public void delete(UUID id) {
        recipeRepository.deleteById(id);
    }
}
