package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.RecipeFoodDTO;
import com.planifAI.diet_service.service.RecipeFoodService;
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

@WebMvcTest(RecipeFoodController.class)
class RecipeFoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeFoodService recipeFoodService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecipeFoodDTO recipeFoodDTO;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        recipeFoodDTO = new RecipeFoodDTO();
        recipeFoodDTO.setId(id);
        recipeFoodDTO.setRecipeId(UUID.randomUUID());
        recipeFoodDTO.setFoodId(UUID.randomUUID());
        recipeFoodDTO.setQuantityGramsMl(200.0);
        recipeFoodDTO.setNotes("Use whole grain rice");
    }

    @Test
    void findAll_ShouldReturnListOfRecipeFoods() throws Exception {
        Mockito.when(recipeFoodService.findAll()).thenReturn(List.of(recipeFoodDTO));

        mockMvc.perform(get("/api/diet/recipe-foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notes").value("Use whole grain rice"));
    }

    @Test
    void findById_ShouldReturnRecipeFood() throws Exception {
        Mockito.when(recipeFoodService.findById(id)).thenReturn(recipeFoodDTO);

        mockMvc.perform(get("/api/diet/recipe-foods/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Use whole grain rice"));
    }

    @Test
    void create_ShouldReturnCreatedRecipeFood() throws Exception {
        Mockito.when(recipeFoodService.create(any(RecipeFoodDTO.class))).thenReturn(recipeFoodDTO);

        mockMvc.perform(post("/api/diet/recipe-foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeFoodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Use whole grain rice"));
    }

    @Test
    void update_ShouldReturnUpdatedRecipeFood() throws Exception {
        Mockito.when(recipeFoodService.update(eq(id), any(RecipeFoodDTO.class))).thenReturn(recipeFoodDTO);

        mockMvc.perform(put("/api/diet/recipe-foods/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeFoodDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Use whole grain rice"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/diet/recipe-foods/{id}", id))
                .andExpect(status().isNoContent());
    }
}
