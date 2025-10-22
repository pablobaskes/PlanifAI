package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.RecipeV2DTO;
import com.planifAI.diet_service.model.RecipeV2;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipeFoodMapper.class})
public interface RecipeV2Mapper {
    RecipeV2DTO toDto(RecipeV2 entity);
    RecipeV2 toEntity(RecipeV2DTO dto);
    List<RecipeV2DTO> toDtoList(List<RecipeV2> entities);
    List<RecipeV2> toEntityList(List<RecipeV2DTO> dtos);
}
