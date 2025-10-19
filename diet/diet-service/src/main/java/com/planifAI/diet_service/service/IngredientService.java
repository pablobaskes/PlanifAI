package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.IngredientDTO;
import com.planifAI.diet_service.mapper.IngredientMapper;
import com.planifAI.diet_service.model.Ingredient;
import com.planifAI.diet_service.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientDTO> findAll() {
        return ingredientMapper.toDtoList(ingredientRepository.findAll());
    }

    public IngredientDTO findById(UUID id) {
        return ingredientRepository.findById(id)
                .map(ingredientMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
    }

    public IngredientDTO create(IngredientDTO dto) {
        Ingredient entity = ingredientMapper.toEntity(dto);
        return ingredientMapper.toDto(ingredientRepository.save(entity));
    }

    public IngredientDTO update(UUID id, IngredientDTO dto) {
        Ingredient entity = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        ingredientMapper.updateEntityFromDto(dto, entity);
        return ingredientMapper.toDto(ingredientRepository.save(entity));
    }

    public void delete(UUID id) {
        ingredientRepository.deleteById(id);
    }
}
