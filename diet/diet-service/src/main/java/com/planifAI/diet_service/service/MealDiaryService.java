package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.MealDiaryDTO;
import com.planifAI.diet_service.mapper.MealDiaryMapper;
import com.planifAI.diet_service.model.MealDiary;
import com.planifAI.diet_service.repository.MealDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealDiaryService {

    private final MealDiaryRepository mealDiaryRepository;
    private final MealDiaryMapper mealDiaryMapper;

    public List<MealDiaryDTO> findAll() {
        return mealDiaryMapper.toDtoList(mealDiaryRepository.findAll());
    }

    public MealDiaryDTO findById(UUID id) {
        return mealDiaryRepository.findById(id)
                .map(mealDiaryMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Meal diary not found"));
    }

    public List<MealDiaryDTO> findByUserId(UUID userId) {
        return mealDiaryMapper.toDtoList(mealDiaryRepository.findByUserId(userId));
    }

    public MealDiaryDTO create(MealDiaryDTO dto) {
        MealDiary entity = mealDiaryMapper.toEntity(dto);
        return mealDiaryMapper.toDto(mealDiaryRepository.save(entity));
    }

    public MealDiaryDTO update(UUID id, MealDiaryDTO dto) {
        MealDiary entity = mealDiaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal diary not found"));

        mealDiaryMapper.updateEntityFromDto(dto, entity);
        return mealDiaryMapper.toDto(mealDiaryRepository.save(entity));
    }

    public void delete(UUID id) {
        mealDiaryRepository.deleteById(id);
    }
}
