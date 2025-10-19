package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.IngredientDTO;
import com.planifAI.diet_service.model.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientDTO toDto(Ingredient ingredient);

    Ingredient toEntity(IngredientDTO dto);

    List<IngredientDTO> toDtoList(List<Ingredient> ingredients);

    void updateEntityFromDto(IngredientDTO dto, @MappingTarget Ingredient entity);

}
