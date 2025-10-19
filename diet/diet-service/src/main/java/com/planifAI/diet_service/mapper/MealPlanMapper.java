package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.MealPlanDTO;
import com.planifAI.diet_service.model.MealPlan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    MealPlanDTO toDto(MealPlan plan);

    MealPlan toEntity(MealPlanDTO dto);

    List<MealPlanDTO> toDtoList(List<MealPlan> plans);
}
