package com.planifAI.auth_service.dto;

import com.planifAI.auth_service.model.User; // Asegúrate de usar la ruta correcta a tu entidad User


public record RefreshTokenWithRawToken(User user, String rawToken) {
}
