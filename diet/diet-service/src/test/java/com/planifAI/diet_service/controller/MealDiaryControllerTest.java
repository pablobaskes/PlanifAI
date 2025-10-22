package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.MealDiaryDTO;
import com.planifAI.diet_service.service.MealDiaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealDiaryController.class)
class MealDiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealDiaryService mealDiaryService;

    @Autowired
    private ObjectMapper objectMapper;

    private MealDiaryDTO sampleDiary;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        sampleDiary = new MealDiaryDTO();
        sampleDiary.setId(id);
        sampleDiary.setUserId(UUID.randomUUID());
        sampleDiary.setDateTime(LocalDateTime.now());
        sampleDiary.setMealType("Lunch");
        sampleDiary.setTotalCalories(650.0);
    }

    @Test
    void shouldReturnAllMealDiaries() throws Exception {
        Mockito.when(mealDiaryService.findAll()).thenReturn(List.of(sampleDiary));

        mockMvc.perform(get("/api/diet/meal-diary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mealType").value("Lunch"));
    }

    @Test
    void shouldReturnMealDiaryById() throws Exception {
        Mockito.when(mealDiaryService.findById(id)).thenReturn(sampleDiary);

        mockMvc.perform(get("/api/diet/meal-diary/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(650.0));
    }

    @Test
    void shouldCreateMealDiary() throws Exception {
        Mockito.when(mealDiaryService.create(any(MealDiaryDTO.class))).thenReturn(sampleDiary);

        mockMvc.perform(post("/api/diet/meal-diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDiary)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealType").value("Lunch"));
    }

    @Test
    void shouldUpdateMealDiary() throws Exception {
        Mockito.when(mealDiaryService.update(eq(id), any(MealDiaryDTO.class))).thenReturn(sampleDiary);

        mockMvc.perform(put("/api/diet/meal-diary/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDiary)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(650.0));
    }

    @Test
    void shouldDeleteMealDiary() throws Exception {
        mockMvc.perform(delete("/api/diet/meal-diary/{id}", id))
                .andExpect(status().isNoContent());
    }
}
