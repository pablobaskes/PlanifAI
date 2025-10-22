package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.NutritionalGoalDTO;
import com.planifAI.diet_service.mapper.NutritionalGoalMapper;
import com.planifAI.diet_service.model.NutritionalGoal;
import com.planifAI.diet_service.repository.NutritionalGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NutritionalGoalService {

    private final NutritionalGoalRepository goalRepository;
    private final NutritionalGoalMapper goalMapper;

    public List<NutritionalGoalDTO> findAll() {
        return goalMapper.toDtoList(goalRepository.findAll());
    }

    public NutritionalGoalDTO findById(UUID id) {
        return goalRepository.findById(id)
                .map(goalMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Nutritional goal not found"));
    }

    public List<NutritionalGoalDTO> findByUserId(UUID userId) {
        return goalMapper.toDtoList(goalRepository.findByUserId(userId));
    }

    public NutritionalGoalDTO create(NutritionalGoalDTO dto) {
        NutritionalGoal entity = goalMapper.toEntity(dto);
        return goalMapper.toDto(goalRepository.save(entity));
    }

    public NutritionalGoalDTO update(UUID id, NutritionalGoalDTO dto) {
        NutritionalGoal entity = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nutritional goal not found"));
        goalMapper.updateEntityFromDto(dto, entity);
        return goalMapper.toDto(goalRepository.save(entity));
    }

    public void delete(UUID id) {
        goalRepository.deleteById(id);
    }
}
