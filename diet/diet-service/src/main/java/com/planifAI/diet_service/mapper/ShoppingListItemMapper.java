package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.ShoppingListItemDto;
import com.planifAI.diet_service.model.ShoppingListItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShoppingListItemMapper {

    ShoppingListItemDto toDto(ShoppingListItem item);

    ShoppingListItem toEntity(ShoppingListItemDto dto);

    List<ShoppingListItemDto> toDtoList(List<ShoppingListItem> items);
}
