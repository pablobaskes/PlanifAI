package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.DietaryRestrictionDTO;
import com.planifAI.diet_service.mapper.DietaryRestrictionMapper;
import com.planifAI.diet_service.model.DietaryRestriction;
import com.planifAI.diet_service.repository.DietaryRestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DietaryRestrictionService {

    private final DietaryRestrictionRepository restrictionRepository;
    private final DietaryRestrictionMapper restrictionMapper;

    public List<DietaryRestrictionDTO> findAll() {
        return restrictionMapper.toDtoList(restrictionRepository.findAll());
    }

    public DietaryRestrictionDTO findById(UUID id) {
        return restrictionRepository.findById(id)
                .map(restrictionMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Dietary restriction not found"));
    }

    public List<DietaryRestrictionDTO> findByUserId(UUID userId) {
        return restrictionMapper.toDtoList(restrictionRepository.findByUserId(userId));
    }

    public DietaryRestrictionDTO create(DietaryRestrictionDTO dto) {
        DietaryRestriction entity = restrictionMapper.toEntity(dto);
        return restrictionMapper.toDto(restrictionRepository.save(entity));
    }

    public DietaryRestrictionDTO update(UUID id, DietaryRestrictionDTO dto) {
        DietaryRestriction entity = restrictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dietary restriction not found"));

        restrictionMapper.updateEntityFromDto(dto, entity);
        return restrictionMapper.toDto(restrictionRepository.save(entity));
    }

    public void delete(UUID id) {
        restrictionRepository.deleteById(id);
    }
}
