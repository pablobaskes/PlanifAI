package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.ProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgressRecordRepository extends JpaRepository<ProgressRecord, UUID> {
    List<ProgressRecord> findByUserId(UUID userId);
}
