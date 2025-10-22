package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.RecipeV2DTO;
import com.planifAI.diet_service.mapper.RecipeV2Mapper;
import com.planifAI.diet_service.model.RecipeV2;
import com.planifAI.diet_service.repository.RecipeV2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeV2Service {

    private final RecipeV2Repository recipeDBRepository;
    private final RecipeV2Mapper recipeDBMapper;

    public List<RecipeV2DTO> findAll() {
        return recipeDBMapper.toDtoList(recipeDBRepository.findAll());
    }

    public RecipeV2DTO findById(UUID id) {
        return recipeDBRepository.findById(id)
                .map(recipeDBMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public RecipeV2DTO create(RecipeV2DTO dto) {
        RecipeV2 entity = recipeDBMapper.toEntity(dto);
        return recipeDBMapper.toDto(recipeDBRepository.save(entity));
    }

    public RecipeV2DTO update(UUID id, RecipeV2DTO dto) {
        RecipeV2 entity = recipeDBRepository.findById(id)
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
