package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.NutritionalGoalDTO;
import com.planifAI.diet_service.service.NutritionalGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NutritionalGoalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NutritionalGoalService goalService;

    @InjectMocks
    private NutritionalGoalController goalController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private NutritionalGoalDTO sampleGoal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(goalController).build();

        sampleGoal = new NutritionalGoalDTO();
        sampleGoal.setId(UUID.randomUUID());
        sampleGoal.setUserId(UUID.randomUUID());
        sampleGoal.setMainGoal("Lose Weight");
        sampleGoal.setDailyCalories(2200.0);
        sampleGoal.setProteinGrams(150.0);
        sampleGoal.setCarbsGrams(250.0);
        sampleGoal.setFatGrams(60.0);
    }

    @Test
    void testFindAll() throws Exception {
        when(goalService.findAll()).thenReturn(Collections.singletonList(sampleGoal));

        mockMvc.perform(get("/api/diet/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mainGoal").value("Lose Weight"));
    }

    @Test
    void testFindById() throws Exception {
        when(goalService.findById(any(UUID.class))).thenReturn(sampleGoal);

        mockMvc.perform(get("/api/diet/goals/{id}", sampleGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caloriesPerDay").value(2200.0));
    }

    @Test
    void testCreate() throws Exception {
        when(goalService.create(any(NutritionalGoalDTO.class))).thenReturn(sampleGoal);

        mockMvc.perform(post("/api/diet/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGoal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proteinGrams").value(150.0));
    }

    @Test
    void testUpdate() throws Exception {
        when(goalService.update(any(UUID.class), any(NutritionalGoalDTO.class))).thenReturn(sampleGoal);

        mockMvc.perform(put("/api/diet/goals/{id}", sampleGoal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGoal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fatGrams").value(60.0));
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/diet/goals/{id}", sampleGoal.getId()))
                .andExpect(status().isNoContent());
    }
}
