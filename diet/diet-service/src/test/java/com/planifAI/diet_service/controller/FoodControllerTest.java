package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.FoodDTO;
import com.planifAI.diet_service.service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(FoodController.class)
class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FoodService foodService;

    @Autowired
    private ObjectMapper objectMapper;

    private FoodDTO foodDTO;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        foodDTO = new FoodDTO();
        foodDTO.setId(id);
        foodDTO.setName("Chicken Breast");
        foodDTO.setCaloriesPer100g(165.0);
        foodDTO.setProteinPer100g(31.0);
        foodDTO.setCarbsPer100g(0.0);
        foodDTO.setFatPer100g(3.6);
        foodDTO.setPortionUnit("g");
    }

    @Test
    void findAll_ShouldReturnListOfFoods() throws Exception {
        Mockito.when(foodService.findAll()).thenReturn(List.of(foodDTO));

        mockMvc.perform(get("/api/diet/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Chicken Breast"));
    }

    @Test
    void findById_ShouldReturnFood() throws Exception {
        Mockito.when(foodService.findById(id)).thenReturn(foodDTO);

        mockMvc.perform(get("/api/diet/foods/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Breast"));
    }

    @Test
    void create_ShouldReturnCreatedFood() throws Exception {
        Mockito.when(foodService.create(any(FoodDTO.class))).thenReturn(foodDTO);

        mockMvc.perform(post("/api/diet/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Breast"));
    }

    @Test
    void update_ShouldReturnUpdatedFood() throws Exception {
        Mockito.when(foodService.update(eq(id), any(FoodDTO.class))).thenReturn(foodDTO);

        mockMvc.perform(put("/api/diet/foods/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Breast"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/diet/foods/{id}", id))
                .andExpect(status().isNoContent());
    }
}
