package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.ShoppingListDTO;
import com.planifAI.diet_service.model.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ShoppingListItemMapper.class })
public interface ShoppingListMapper {

    @Mapping(target = "items", source = "items")
    ShoppingListDTO toDto(ShoppingList list);

    ShoppingList toEntity(ShoppingListDTO dto);

    List<ShoppingListDTO> toDtoList(List<ShoppingList> lists);

    void updateEntityFromDto(ShoppingListDTO dto, @MappingTarget ShoppingList entity);

}
