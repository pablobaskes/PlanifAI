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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecipeDTO recipe;

    @BeforeEach
    void setUp() {
        recipe = new RecipeDTO();
        recipe.setId(UUID.randomUUID());
        recipe.setName("Pasta Carbonara");
        recipe.setInstructions("Boil pasta, mix with sauce");
    }

    @Test
    void shouldReturnAllRecipes() throws Exception {
        Mockito.when(recipeService.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/diet/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pasta Carbonara"));
    }

    @Test
    void shouldReturnRecipeById() throws Exception {
        Mockito.when(recipeService.findById(recipe.getId())).thenReturn(recipe);

        mockMvc.perform(get("/api/diet/recipes/{id}", recipe.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta Carbonara"));
    }

    @Test
    void shouldCreateRecipe() throws Exception {
        Mockito.when(recipeService.create(any(RecipeDTO.class))).thenReturn(recipe);

        mockMvc.perform(post("/api/diet/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta Carbonara"));
    }

    @Test
    void shouldUpdateRecipe() throws Exception {
        Mockito.when(recipeService.update(eq(recipe.getId()), any(RecipeDTO.class))).thenReturn(recipe);

        mockMvc.perform(put("/api/diet/recipes/{id}", recipe.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta Carbonara"));
    }

    @Test
    void shouldDeleteRecipe() throws Exception {
        mockMvc.perform(delete("/api/diet/recipes/{id}", recipe.getId()))
                .andExpect(status().isNoContent());
    }
}
