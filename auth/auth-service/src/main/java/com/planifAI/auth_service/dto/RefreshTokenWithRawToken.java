package com.planifAI.auth_service.dto;

import com.planifAI.auth_service.model.User;


public record RefreshTokenWithRawToken(User user, String rawToken) {
}
