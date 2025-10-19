package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.ShoppingListDto;
import com.planifAI.diet_service.model.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ShoppingListItemMapper.class })
public interface ShoppingListMapper {

    @Mapping(target = "items", source = "items")
    ShoppingListDto toDto(ShoppingList list);

    ShoppingList toEntity(ShoppingListDto dto);

    List<ShoppingListDto> toDtoList(List<ShoppingList> lists);
}
