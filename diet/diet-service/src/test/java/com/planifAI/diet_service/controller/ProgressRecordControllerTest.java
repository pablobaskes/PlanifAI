package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.ProgressRecordDTO;
import com.planifAI.diet_service.service.ProgressRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProgressRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProgressRecordService recordService;

    @InjectMocks
    private ProgressRecordController recordController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProgressRecordDTO sampleRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recordController).build();

        sampleRecord = new ProgressRecordDTO();
        sampleRecord.setId(UUID.randomUUID());
        sampleRecord.setUserId(UUID.randomUUID());
        sampleRecord.setDate(LocalDate.of(2025, 10, 21));
        sampleRecord.setWeightKg(72.5);
        sampleRecord.setBodyFatPercentage(18.0);
        sampleRecord.setWaterIntakeMl(2000.0);
    }

    @Test
    void testFindAll() throws Exception {
        when(recordService.findAll()).thenReturn(Collections.singletonList(sampleRecord));

        mockMvc.perform(get("/api/diet/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weightKg").value(72.5));
    }

    @Test
    void testFindById() throws Exception {
        when(recordService.findById(any(UUID.class))).thenReturn(sampleRecord);

        mockMvc.perform(get("/api/diet/progress/{id}", sampleRecord.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bodyFatPercentage").value(18.0));
    }

    @Test
    void testCreate() throws Exception {
        when(recordService.create(any(ProgressRecordDTO.class))).thenReturn(sampleRecord);

        mockMvc.perform(post("/api/diet/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRecord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waterIntakeMl").value(2000.0));
    }

    @Test
    void testUpdate() throws Exception {
        when(recordService.update(any(UUID.class), any(ProgressRecordDTO.class))).thenReturn(sampleRecord);

        mockMvc.perform(put("/api/diet/progress/{id}", sampleRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRecord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(72.5));
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/diet/progress/{id}", sampleRecord.getId()))
                .andExpect(status().isNoContent());
    }
}
