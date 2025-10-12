package com.planifAI.auth_service.service;


import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registro de usuario (delegado a UserService)
     */
    @Transactional
    public User register(String username, String email, String password) {
        return userService.registerUser(username, email, password);
    }

    /**
     * Login: valida credenciales, emite JWT y refresh token.
     */
    @Transactional
    public Map<String, Object> login(String identifier, String password) {
        User user = userService.findByEmailOrUsername(identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, jwtTokenProvider.getRefreshTokenExpirySeconds());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getTokenHash(), // raw token se devuelve aquí
                "expiresIn", jwtTokenProvider.getAccessTokenExpirySeconds()
        );
    }

    /**
     * Logout (revoca refresh token)
     */
    @Transactional
    public void logout(String refreshTokenRaw) {
        refreshTokenService.validateAndGet(refreshTokenRaw)
                .ifPresent(refreshTokenService::revokeToken);
    }
}
