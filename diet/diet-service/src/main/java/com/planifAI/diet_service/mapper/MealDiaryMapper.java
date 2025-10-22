package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.MealDiaryDTO;
import com.planifAI.diet_service.model.MealDiary;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MealDiaryMapper {

    MealDiaryDTO toDto(MealDiary entity);

    MealDiary toEntity(MealDiaryDTO dto);

    List<MealDiaryDTO> toDtoList(List<MealDiary> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MealDiaryDTO dto, @MappingTarget MealDiary entity);
}
