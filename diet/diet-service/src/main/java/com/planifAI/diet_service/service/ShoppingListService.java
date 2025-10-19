package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.ShoppingListDTO;
import com.planifAI.diet_service.mapper.ShoppingListMapper;
import com.planifAI.diet_service.model.ShoppingList;
import com.planifAI.diet_service.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper shoppingListMapper;

    public List<ShoppingListDTO> findAll() {
        return shoppingListMapper.toDtoList(shoppingListRepository.findAll());
    }

    public ShoppingListDTO create(ShoppingListDTO dto) {
        ShoppingList list = shoppingListMapper.toEntity(dto);
        return shoppingListMapper.toDto(shoppingListRepository.save(list));
    }

    public void delete(UUID id) {
        shoppingListRepository.deleteById(id);
    }
}

