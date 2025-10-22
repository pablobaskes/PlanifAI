package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.RecipeDTO;
import com.planifAI.diet_service.service.RecipeService;
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

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeDBService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecipeDTO recipeDBDTO;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        recipeDBDTO = new RecipeDTO();
        recipeDBDTO.setId(id);
        recipeDBDTO.setName("Protein Pancakes");
        recipeDBDTO.setPreparationTimeMin(15);
        recipeDBDTO.setInstructions("Mix ingredients and cook.");
        recipeDBDTO.setServings(2);
        recipeDBDTO.setMealType("Breakfast");
        recipeDBDTO.setDietaryRestrictions("High Protein");
    }

    @Test
    void findAll_ShouldReturnListOfRecipes() throws Exception {
        Mockito.when(recipeDBService.findAll()).thenReturn(List.of(recipeDBDTO));

        mockMvc.perform(get("/api/diet/recipes-db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Protein Pancakes"));
    }

    @Test
    void findById_ShouldReturnRecipe() throws Exception {
        Mockito.when(recipeDBService.findById(id)).thenReturn(recipeDBDTO);

        mockMvc.perform(get("/api/diet/recipes-db/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Protein Pancakes"));
    }

    @Test
    void create_ShouldReturnCreatedRecipe() throws Exception {
        Mockito.when(recipeDBService.create(any(RecipeDTO.class))).thenReturn(recipeDBDTO);

        mockMvc.perform(post("/api/diet/recipes-db")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeDBDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Protein Pancakes"));
    }

    @Test
    void update_ShouldReturnUpdatedRecipe() throws Exception {
        Mockito.when(recipeDBService.update(eq(id), any(RecipeDTO.class))).thenReturn(recipeDBDTO);

        mockMvc.perform(put("/api/diet/recipes-db/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeDBDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Protein Pancakes"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/diet/recipes-db/{id}", id))
                .andExpect(status().isNoContent());
    }
}
