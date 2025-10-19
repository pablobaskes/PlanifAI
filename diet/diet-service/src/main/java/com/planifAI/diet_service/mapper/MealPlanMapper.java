package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.MealPlanDto;
import com.planifAI.diet_service.model.MealPlan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    MealPlanDto toDto(MealPlan plan);

    MealPlan toEntity(MealPlanDto dto);

    List<MealPlanDto> toDtoList(List<MealPlan> plans);
}
