package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.MealPlanDTO;
import com.planifAI.diet_service.service.MealPlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MealPlanController.class)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MealPlanService mealPlanService;

    @Test
    @DisplayName("GET /api/diet/meal-plans")
    void shouldReturnAllMealPlans() throws Exception {
        MealPlanDTO dto = new MealPlanDTO();
        dto.setId(UUID.randomUUID());

        BDDMockito.given(mealPlanService.findAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/diet/meal-plans"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/diet/meal-plans/{id}")
    void shouldReturnMealPlanById() throws Exception {
        UUID id = UUID.randomUUID();
        MealPlanDTO dto = new MealPlanDTO();
        dto.setId(id);

        BDDMockito.given(mealPlanService.findById(id)).willReturn(dto);

        mockMvc.perform(get("/api/diet/meal-plans/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/diet/meal-plans")
    void shouldCreateMealPlan() throws Exception {
        MealPlanDTO dto = new MealPlanDTO();
        dto.setMealType("Plan Mediterráneo");

        MealPlanDTO saved = new MealPlanDTO();
        saved.setId(UUID.randomUUID());
        saved.setMealType(dto.getMealType());

        BDDMockito.given(mealPlanService.create(any(MealPlanDTO.class))).willReturn(saved);

        mockMvc.perform(post("/api/diet/meal-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/diet/meal-plans/{id}")
    void shouldUpdateMealPlan() throws Exception {
        UUID id = UUID.randomUUID();

        MealPlanDTO dto = new MealPlanDTO();
        dto.setMealType("Plan detox");

        MealPlanDTO updated = new MealPlanDTO();
        updated.setId(id);
        updated.setMealType("Plan detox");

        BDDMockito.given(mealPlanService.update(eq(id), any(MealPlanDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/diet/meal-plans/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/diet/meal-plans/{id}")
    void shouldDeleteMealPlan() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/diet/meal-plans/{id}", id))
                .andExpect(status().isNoContent());
    }
}
