package com.planifAI.diet_service.mapper;

import com.planifAI.diet_service.dto.ProgressRecordDTO;
import com.planifAI.diet_service.model.ProgressRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProgressRecordMapper {

    ProgressRecordDTO toDto(ProgressRecord entity);

    ProgressRecord toEntity(ProgressRecordDTO dto);

    List<ProgressRecordDTO> toDtoList(List<ProgressRecord> entities);

    void updateEntityFromDto(ProgressRecordDTO dto, @MappingTarget ProgressRecord entity);
}
