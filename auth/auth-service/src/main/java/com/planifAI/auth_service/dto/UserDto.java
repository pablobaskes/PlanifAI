package com.planifAI.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private Set<String> roles;

    // --- Campos de Perfil Extendidos ---
    private String nombre;
    private LocalDate fechaNacimiento;
    private String genero;
    private String nivelActividad;
    private String unidadMedida;

    // --- Campo de Configuración ---
    private boolean autoUpdatePantry;
}