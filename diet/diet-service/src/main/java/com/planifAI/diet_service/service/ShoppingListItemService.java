package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.ShoppingListItemDTO;
import com.planifAI.diet_service.mapper.ShoppingListItemMapper;
import com.planifAI.diet_service.model.ShoppingListItem;
import com.planifAI.diet_service.repository.ShoppingListItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingListItemService {

    private final ShoppingListItemRepository itemRepository;
    private final ShoppingListItemMapper itemMapper;

    public List<ShoppingListItemDTO> findAll() {
        return itemMapper.toDtoList(itemRepository.findAll());
    }

    public ShoppingListItemDTO findById(UUID id) {
        return itemRepository.findById(id)
                .map(itemMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Shopping list item not found"));
    }

    public ShoppingListItemDTO create(ShoppingListItemDTO dto) {
        ShoppingListItem entity = itemMapper.toEntity(dto);
        return itemMapper.toDto(itemRepository.save(entity));
    }

    public ShoppingListItemDTO update(UUID id, ShoppingListItemDTO dto) {
        ShoppingListItem entity = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shopping list item not found"));

        itemMapper.updateEntityFromDto(dto, entity);
        return itemMapper.toDto(itemRepository.save(entity));
    }

    public void delete(UUID id) {
        itemRepository.deleteById(id);
    }
}
