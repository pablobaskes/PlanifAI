package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.ShoppingListItemDTO;
import com.planifAI.diet_service.model.ShoppingListItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShoppingListItemMapper {

    ShoppingListItemDTO toDto(ShoppingListItem item);

    ShoppingListItem toEntity(ShoppingListItemDTO dto);

    List<ShoppingListItemDTO> toDtoList(List<ShoppingListItem> items);

    void updateEntityFromDto(ShoppingListItemDTO dto, @MappingTarget ShoppingListItem entity);

}
