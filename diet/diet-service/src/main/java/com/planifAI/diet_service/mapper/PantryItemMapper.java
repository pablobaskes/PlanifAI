package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.PantryItemDTO;
import com.planifAI.diet_service.model.PantryItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PantryItemMapper {

    PantryItemDTO toDto(PantryItem item);

    PantryItem toEntity(PantryItemDTO dto);

    List<PantryItemDTO> toDtoList(List<PantryItem> items);

    void updateEntityFromDto(PantryItemDTO dto, @MappingTarget PantryItem entity);

}
