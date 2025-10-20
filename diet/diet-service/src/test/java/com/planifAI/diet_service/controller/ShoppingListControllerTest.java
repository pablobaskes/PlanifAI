package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.ShoppingListDTO;
import com.planifAI.diet_service.service.ShoppingListService;
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

@WebMvcTest(controllers = ShoppingListController.class)
class ShoppingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoppingListService shoppingListService;

    @Test
    @DisplayName("GET /api/diet/shopping-lists")
    void shouldReturnAllLists() throws Exception {
        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setId(UUID.randomUUID());
        dto.setName("Compra semanal");

        BDDMockito.given(shoppingListService.findAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/diet/shopping-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Compra semanal"));
    }

    @Test
    @DisplayName("GET /api/diet/shopping-lists/{id}")
    void shouldReturnListById() throws Exception {
        UUID id = UUID.randomUUID();
        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setId(id);
        dto.setName("Lista de frutas");

        BDDMockito.given(shoppingListService.findById(id)).willReturn(dto);

        mockMvc.perform(get("/api/diet/shopping-lists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lista de frutas"));
    }

    @Test
    @DisplayName("POST /api/diet/shopping-lists")
    void shouldCreateList() throws Exception {
        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setName("Lista nueva");

        ShoppingListDTO saved = new ShoppingListDTO();
        saved.setId(UUID.randomUUID());
        saved.setName(dto.getName());

        BDDMockito.given(shoppingListService.create(any(ShoppingListDTO.class))).willReturn(saved);

        mockMvc.perform(post("/api/diet/shopping-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lista nueva"));
    }

    @Test
    @DisplayName("PUT /api/diet/shopping-lists/{id}")
    void shouldUpdateList() throws Exception {
        UUID id = UUID.randomUUID();

        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setName("Lista actualizada");

        ShoppingListDTO updated = new ShoppingListDTO();
        updated.setId(id);
        updated.setName("Lista actualizada");

        BDDMockito.given(shoppingListService.update(eq(id), any(ShoppingListDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/diet/shopping-lists/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lista actualizada"));
    }

    @Test
    @DisplayName("DELETE /api/diet/shopping-lists/{id}")
    void shouldDeleteList() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/diet/shopping-lists/{id}", id))
                .andExpect(status().isNoContent());
    }
}

