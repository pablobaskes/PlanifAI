package com.planifAI.auth_service.service;

import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpass")
                .enabled(true)
                .build();
    }

    @Test
    void createRefreshToken_ShouldGenerateAndSaveToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(testUser, 3600);

        assertThat(token).isNotNull();
        assertThat(token.getUser()).isEqualTo(testUser);
        assertThat(token.getTokenHash()).isNotBlank(); // raw token returned
        assertThat(token.getIssuedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void validateAndGet_ShouldReturnTokenIfValidAndNotExpired() {
        RefreshToken storedToken = RefreshToken.builder()
                .user(testUser)
                .tokenHash(Base64.getEncoder().encodeToString("hashed".getBytes()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(storedToken));

        Optional<RefreshToken> result = refreshTokenService.validateAndGet("raw-token");

        assertThat(result).isPresent();
        assertThat(result.get().isRevoked()).isFalse();
        verify(refreshTokenRepository, times(1)).findByTokenHash(anyString());
    }

    @Test
    void validateAndGet_ShouldReturnEmptyIfExpired() {
        RefreshToken expiredToken = RefreshToken.builder()
                .user(testUser)
                .tokenHash("hashed")
                .issuedAt(Instant.now().minusSeconds(4000))
                .expiresAt(Instant.now().minusSeconds(10))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expiredToken));

        Optional<RefreshToken> result = refreshTokenService.validateAndGet("raw-token");

        assertThat(result).isEmpty();
    }

    @Test
    void revokeToken_ShouldMarkTokenAsRevokedAndSave() {
        RefreshToken token = RefreshToken.builder()
                .user(testUser)
                .revoked(false)
                .build();

        refreshTokenService.revokeToken(token);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(token);
    }

    @Test
    void revokeAllUserTokens_ShouldRevokeAllUserTokens() {
        RefreshToken token1 = RefreshToken.builder().revoked(false).build();
        RefreshToken token2 = RefreshToken.builder().revoked(false).build();

        when(refreshTokenRepository.findByUser(testUser)).thenReturn(List.of(token1, token2));

        refreshTokenService.revokeAllUserTokens(testUser);

        assertThat(token1.isRevoked()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }
}
