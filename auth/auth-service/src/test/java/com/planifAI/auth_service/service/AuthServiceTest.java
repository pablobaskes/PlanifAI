package com.planifAI.auth_service.service;

import com.planifAI.auth_service.dto.LoginRequest;
import com.planifAI.auth_service.dto.LoginResponse;
import com.planifAI.auth_service.dto.RegisterRequest;
import com.planifAI.auth_service.dto.UserDto;
import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.Role;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPass")
                .enabled(true)
                .roles(Set.of(Role.builder().id(1).name("ROLE_USER").build()))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void register_ShouldCallUserServiceAndReturnUserDto() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password");

        when(userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword()))
                .thenReturn(testUser);

        UserDto result = authService.register(request);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRoles()).contains("ROLE_USER");
        verify(userService, times(1)).registerUser(anyString(), anyString(), anyString());
    }

    @Test
    void login_ShouldThrowExceptionIfUserNotFound() {
        LoginRequest request = new LoginRequest("notfound", "password");

        when(userService.findByEmailOrUsername(request.getUsernameOrEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_ShouldThrowExceptionIfPasswordInvalid() {
        LoginRequest request = new LoginRequest("testuser", "wrongpass");

        when(userService.findByEmailOrUsername(request.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_ShouldReturnTokensIfCredentialsValid() {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(userService.findByEmailOrUsername(request.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser))
                .thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshTokenExpirySeconds()).thenReturn(86400L);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash("refresh-token")
                .user(testUser)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400))
                .revoked(false)
                .build();

        when(refreshTokenService.createRefreshToken(testUser, 86400L))
                .thenReturn(refreshToken.getTokenHash());

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    void logout_ShouldRevokeTokenIfValid() {
        String rawToken = "some-refresh-token";
        RefreshToken token = RefreshToken.builder()
                .tokenHash("hashed")
                .user(testUser)
                .revoked(false)
                .build();

        when(refreshTokenService.validateAndGet(rawToken))
                .thenReturn(Optional.of(token));

        authService.logout(rawToken);

        verify(refreshTokenService, times(1)).revokeToken(token);
    }

    @Test
    void logout_ShouldDoNothingIfTokenNotFound() {
        when(refreshTokenService.validateAndGet(anyString()))
                .thenReturn(Optional.empty());

        authService.logout("unknown");

        verify(refreshTokenService, never()).revokeToken(any());
    }
}
