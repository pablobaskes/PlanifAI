package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.NutritionalGoalDTO;
import com.planifAI.diet_service.model.NutritionalGoal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NutritionalGoalMapper {

    NutritionalGoalDTO toDto(NutritionalGoal entity);

    NutritionalGoal toEntity(NutritionalGoalDTO dto);

    List<NutritionalGoalDTO> toDtoList(List<NutritionalGoal> entities);

    void updateEntityFromDto(NutritionalGoalDTO dto, @MappingTarget NutritionalGoal entity);
}
