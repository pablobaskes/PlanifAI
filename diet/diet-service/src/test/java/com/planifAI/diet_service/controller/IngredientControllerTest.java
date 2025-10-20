package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.IngredientDTO;
import com.planifAI.diet_service.service.IngredientService;
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

@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngredientService ingredientService;

    @Autowired
    private ObjectMapper objectMapper;

    private IngredientDTO sample;

    @BeforeEach
    void setUp() {
        sample = new IngredientDTO();
        sample.setId(UUID.randomUUID());
        sample.setName("Tomato");
        sample.setUnit("g");
        sample.setQuantity(100.0);
    }

    @Test
    void shouldReturnAllIngredients() throws Exception {
        Mockito.when(ingredientService.findAll()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/diet/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomato"));
    }

    @Test
    void shouldReturnIngredientById() throws Exception {
        Mockito.when(ingredientService.findById(sample.getId())).thenReturn(sample);

        mockMvc.perform(get("/api/diet/ingredients/{id}", sample.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato"));
    }

    @Test
    void shouldCreateIngredient() throws Exception {
        Mockito.when(ingredientService.create(any(IngredientDTO.class))).thenReturn(sample);

        mockMvc.perform(post("/api/diet/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato"));
    }

    @Test
    void shouldUpdateIngredient() throws Exception {
        Mockito.when(ingredientService.update(eq(sample.getId()), any(IngredientDTO.class))).thenReturn(sample);

        mockMvc.perform(put("/api/diet/ingredients/{id}", sample.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato"));
    }

    @Test
    void shouldDeleteIngredient() throws Exception {
        mockMvc.perform(delete("/api/diet/ingredients/{id}", sample.getId()))
                .andExpect(status().isNoContent());
    }
}
