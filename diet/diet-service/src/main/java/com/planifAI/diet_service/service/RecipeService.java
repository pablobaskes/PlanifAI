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

    private final RecipeRepository recipeDBRepository;
    private final RecipeMapper recipeDBMapper;

    public List<RecipeDTO> findAll() {
        return recipeDBMapper.toDtoList(recipeDBRepository.findAll());
    }

    public RecipeDTO findById(UUID id) {
        return recipeDBRepository.findById(id)
                .map(recipeDBMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public RecipeDTO create(RecipeDTO dto) {
        Recipe entity = recipeDBMapper.toEntity(dto);
        return recipeDBMapper.toDto(recipeDBRepository.save(entity));
    }

    public RecipeDTO update(UUID id, RecipeDTO dto) {
        Recipe entity = recipeDBRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        entity.setName(dto.getName());
        entity.setPreparationTimeMin(dto.getPreparationTimeMin());
        entity.setInstructions(dto.getInstructions());
        entity.setServings(dto.getServings());
        entity.setMealType(dto.getMealType());
        entity.setDietaryRestrictions(dto.getDietaryRestrictions());

        return recipeDBMapper.toDto(recipeDBRepository.save(entity));
    }

    public void delete(UUID id) {
        recipeDBRepository.deleteById(id);
    }
}
