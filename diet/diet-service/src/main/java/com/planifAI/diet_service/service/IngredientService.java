package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.IngredientDto;
import com.planifAI.diet_service.model.Ingredient;
import com.planifAI.diet_service.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = Ingredient.builder()
                .name(dto.getName())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .build();

        Ingredient saved = ingredientRepository.save(ingredient);
        dto.setId(saved.getId());
        return dto;
    }

    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(i -> new IngredientDto(i.getId(), i.getName(), i.getQuantity(), i.getUnit(), i.getUserId()))
                .toList();
    }
}
