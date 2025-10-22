package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.ProgressRecordDTO;
import com.planifAI.diet_service.mapper.ProgressRecordMapper;
import com.planifAI.diet_service.model.ProgressRecord;
import com.planifAI.diet_service.repository.ProgressRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressRecordService {

    private final ProgressRecordRepository recordRepository;
    private final ProgressRecordMapper recordMapper;

    public List<ProgressRecordDTO> findAll() {
        return recordMapper.toDtoList(recordRepository.findAll());
    }

    public ProgressRecordDTO findById(UUID id) {
        return recordRepository.findById(id)
                .map(recordMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Progress record not found"));
    }

    public List<ProgressRecordDTO> findByUserId(UUID userId) {
        return recordMapper.toDtoList(recordRepository.findByUserId(userId));
    }

    public ProgressRecordDTO create(ProgressRecordDTO dto) {
        ProgressRecord entity = recordMapper.toEntity(dto);
        return recordMapper.toDto(recordRepository.save(entity));
    }

    public ProgressRecordDTO update(UUID id, ProgressRecordDTO dto) {
        ProgressRecord entity = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress record not found"));
        recordMapper.updateEntityFromDto(dto, entity);
        return recordMapper.toDto(recordRepository.save(entity));
    }

    public void delete(UUID id) {
        recordRepository.deleteById(id);
    }
}
