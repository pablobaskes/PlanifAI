package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.IngredientDto;
import com.planifAI.diet_service.model.Ingredient;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientDto toDto(Ingredient ingredient);

    Ingredient toEntity(IngredientDto dto);

    List<IngredientDto> toDtoList(List<Ingredient> ingredients);
}
