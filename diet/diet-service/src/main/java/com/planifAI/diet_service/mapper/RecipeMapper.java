package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.RecipeDto;
import com.planifAI.diet_service.model.Recipe;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    RecipeDto toDto(Recipe recipe);

    Recipe toEntity(RecipeDto dto);

    List<RecipeDto> toDtoList(List<Recipe> recipes);
}
