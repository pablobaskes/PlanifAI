package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.FoodDTO;
import com.planifAI.diet_service.model.Food;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodMapper {
    FoodDTO toDto(Food entity);
    Food toEntity(FoodDTO dto);
    List<FoodDTO> toDtoList(List<Food> entities);
    List<Food> toEntityList(List<FoodDTO> dtos);
}
