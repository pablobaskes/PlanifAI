package com.planifAI.auth_service.service;

import com.planifAI.auth_service.dto.LoginRequest;
import com.planifAI.auth_service.dto.LoginResponse;
import com.planifAI.auth_service.dto.RegisterRequest;
import com.planifAI.auth_service.dto.UserDto;
import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registro de usuario
     */
    @Transactional
    public UserDto register(RegisterRequest request) {
        User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles() != null
                        ? user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet())
                        : Set.of()
        );
    }

    /**
     * Login: valida credenciales, emite JWT y refresh token.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmailOrUsername(request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, jwtTokenProvider.getRefreshTokenExpirySeconds());

        return new LoginResponse(
                accessToken,
                refreshToken.getTokenHash(), // aquí puedes devolver el token raw si lo generas así
                jwtTokenProvider.getAccessTokenExpirySeconds()
        );
    }

    /**
     * Logout
     */
    @Transactional
    public void logout(String refreshTokenRaw) {
        refreshTokenService.validateAndGet(refreshTokenRaw)
                .ifPresent(refreshTokenService::revokeToken);
    }
}
