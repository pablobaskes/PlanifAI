package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.model.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    RecipeDTO toDto(Recipe recipe);

    Recipe toEntity(RecipeDTO dto);

    List<RecipeDTO> toDtoList(List<Recipe> recipes);

    void updateEntityFromDto(RecipeDTO dto, @MappingTarget Recipe entity);

}
