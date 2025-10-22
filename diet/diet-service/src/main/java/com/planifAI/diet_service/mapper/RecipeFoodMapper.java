package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.RecipeFoodDTO;
import com.planifAI.diet_service.model.RecipeFood;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeFoodMapper {

    @Mapping(source = "recipe.id", target = "recipeId")
    @Mapping(source = "food.id", target = "foodId")
    RecipeFoodDTO toDto(RecipeFood entity);

    @Mapping(source = "recipeId", target = "recipe.id")
    @Mapping(source = "foodId", target = "food.id")
    RecipeFood toEntity(RecipeFoodDTO dto);

    List<RecipeFoodDTO> toDtoList(List<RecipeFood> entities);
    List<RecipeFood> toEntityList(List<RecipeFoodDTO> dtos);
}
