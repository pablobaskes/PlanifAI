package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.model.Recipe;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipeFoodMapper.class})
public interface RecipeMapper {
    RecipeDTO toDto(Recipe entity);
    Recipe toEntity(RecipeDTO dto);
    List<RecipeDTO> toDtoList(List<Recipe> entities);
    List<Recipe> toEntityList(List<RecipeDTO> dtos);
}
