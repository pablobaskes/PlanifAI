package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.FoodDTO;
import com.planifAI.diet_service.mapper.FoodMapper;
import com.planifAI.diet_service.model.Food;
import com.planifAI.diet_service.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    public List<FoodDTO> findAll() {
        return foodMapper.toDtoList(foodRepository.findAll());
    }

    public FoodDTO findById(UUID id) {
        return foodRepository.findById(id)
                .map(foodMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Food not found"));
    }

    public FoodDTO create(FoodDTO dto) {
        Food entity = foodMapper.toEntity(dto);
        return foodMapper.toDto(foodRepository.save(entity));
    }

    public FoodDTO update(UUID id, FoodDTO dto) {
        Food entity = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found"));
        entity.setName(dto.getName());
        entity.setCaloriesPer100g(dto.getCaloriesPer100g());
        entity.setProteinPer100g(dto.getProteinPer100g());
        entity.setCarbsPer100g(dto.getCarbsPer100g());
        entity.setFatPer100g(dto.getFatPer100g());
        entity.setPortionUnit(dto.getPortionUnit());
        return foodMapper.toDto(foodRepository.save(entity));
    }

    public void delete(UUID id) {
        foodRepository.deleteById(id);
    }
}
