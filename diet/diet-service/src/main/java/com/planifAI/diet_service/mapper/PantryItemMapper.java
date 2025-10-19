package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.PantryItemDto;
import com.planifAI.diet_service.model.PantryItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PantryItemMapper {

    PantryItemDto toDto(PantryItem item);

    PantryItem toEntity(PantryItemDto dto);

    List<PantryItemDto> toDtoList(List<PantryItem> items);
}
