package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.DietaryRestrictionDTO;
import com.planifAI.diet_service.model.DietaryRestriction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietaryRestrictionMapper {

    DietaryRestrictionDTO toDto(DietaryRestriction entity);

    DietaryRestriction toEntity(DietaryRestrictionDTO dto);

    List<DietaryRestrictionDTO> toDtoList(List<DietaryRestriction> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(DietaryRestrictionDTO dto, @MappingTarget DietaryRestriction entity);
}
