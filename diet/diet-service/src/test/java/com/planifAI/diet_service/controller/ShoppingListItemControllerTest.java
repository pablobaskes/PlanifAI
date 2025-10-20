package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.ShoppingListItemDTO;
import com.planifAI.diet_service.service.ShoppingListItemService;
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

@WebMvcTest(controllers = ShoppingListItemController.class)
class ShoppingListItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoppingListItemService itemService;

    @Test
    @DisplayName("GET /api/diet/shopping-list-items")
    void shouldReturnAllItems() throws Exception {
        ShoppingListItemDTO dto = new ShoppingListItemDTO();
        dto.setId(UUID.randomUUID());

        BDDMockito.given(itemService.findAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/diet/shopping-list-items"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/diet/shopping-list-items/{id}")
    void shouldReturnItemById() throws Exception {
        UUID id = UUID.randomUUID();
        ShoppingListItemDTO dto = new ShoppingListItemDTO();
        dto.setId(id);

        BDDMockito.given(itemService.findById(id)).willReturn(dto);

        mockMvc.perform(get("/api/diet/shopping-list-items/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/diet/shopping-list-items")
    void shouldCreateItem() throws Exception {
        ShoppingListItemDTO dto = new ShoppingListItemDTO();
        dto.setIngredientId(UUID.randomUUID());
        dto.setQuantity(7.8);

        ShoppingListItemDTO saved = new ShoppingListItemDTO();
        saved.setId(UUID.randomUUID());
        saved.setIngredientId(dto.getIngredientId());
        saved.setQuantity(dto.getQuantity());

        BDDMockito.given(itemService.create(any(ShoppingListItemDTO.class))).willReturn(saved);

        mockMvc.perform(post("/api/diet/shopping-list-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/diet/shopping-list-items/{id}")
    void shouldUpdateItem() throws Exception {
        UUID id = UUID.randomUUID();

        ShoppingListItemDTO dto = new ShoppingListItemDTO();
        dto.setIngredientId(UUID.randomUUID());
        dto.setQuantity(8.7);

        ShoppingListItemDTO updated = new ShoppingListItemDTO();
        updated.setId(id);
        updated.setIngredientId(UUID.randomUUID());
        updated.setQuantity(4.6);

        BDDMockito.given(itemService.update(eq(id), any(ShoppingListItemDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/diet/shopping-list-items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/diet/shopping-list-items/{id}")
    void shouldDeleteItem() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/diet/shopping-list-items/{id}", id))
                .andExpect(status().isNoContent());
    }
}
