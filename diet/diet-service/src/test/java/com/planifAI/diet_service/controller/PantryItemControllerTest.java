package com.planifAI.diet_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifAI.diet_service.dto.PantryItemDTO;
import com.planifAI.diet_service.service.PantryItemService;
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

@WebMvcTest(PantryItemController.class)
class PantryItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PantryItemService pantryItemService;

    @Autowired
    private ObjectMapper objectMapper;

    private PantryItemDTO item;

    @BeforeEach
    void setUp() {
        item = new PantryItemDTO();
        item.setId(UUID.randomUUID());
        item.setIngredientId(UUID.randomUUID());
        item.setQuantity(12.0);
    }

    @Test
    void shouldReturnAllItems() throws Exception {
        Mockito.when(pantryItemService.findAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/diet/pantry"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnItemById() throws Exception {
        Mockito.when(pantryItemService.findById(item.getId())).thenReturn(item);

        mockMvc.perform(get("/api/diet/pantry/{id}", item.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateItem() throws Exception {
        Mockito.when(pantryItemService.create(any(PantryItemDTO.class))).thenReturn(item);

        mockMvc.perform(post("/api/diet/pantry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        Mockito.when(pantryItemService.update(eq(item.getId()), any(PantryItemDTO.class))).thenReturn(item);

        mockMvc.perform(put("/api/diet/pantry/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteItem() throws Exception {
        mockMvc.perform(delete("/api/diet/pantry/{id}", item.getId()))
                .andExpect(status().isNoContent());
    }
}
