package com.planifAI.auth_service.security;

import com.planifAI.auth_service.model.Role;
import com.planifAI.auth_service.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        String secret = "super-secret-key-for-tests-1234567890";
        jwtTokenProvider = new JwtTokenProvider(secret, "planifai", 60, 3600);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("dummyPasswordHash")
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(
                        Role.builder()
                                .id(1)
                                .name("ROLE_USER")
                                .build()
                ))
                .build();
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        Claims claims = jwtTokenProvider.getClaims(token);

        assertThat(claims.getSubject()).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("username")).isEqualTo("testuser");
        assertThat(claims.get("roles")).asList().contains("ROLE_USER");
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(testUser.getId());

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        Claims claims = jwtTokenProvider.getClaims(token);
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.an.invalid.token";
        boolean result = jwtTokenProvider.validateToken(invalidToken);
        assertThat(result).isFalse();
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(extractedId).isEqualTo(testUser.getId());
    }

    @Test
    void shouldReturnTokenInfo() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        var info = jwtTokenProvider.getTokenInfo(token);

        assertThat(info.get("username")).isEqualTo("testuser");
        assertThat(info.get("roles").toString()).contains("ROLE_USER");
    }

    @Test
    void shouldExpireAccessToken() throws InterruptedException {
        // Access token con 1 segundo de vida
        JwtTokenProvider shortLived = new JwtTokenProvider("another-super-secret-32-bytes-xxxx", "planifai", 1, 3600);
        String token = shortLived.generateAccessToken(testUser);

        // Esperamos 1100 ms para que expire
        Thread.sleep(1100);

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretShouldNotValidate() {
        JwtTokenProvider other = new JwtTokenProvider("different-secret-32-bytes-xxxxxx", "planifai", 60, 3600);
        String token = other.generateAccessToken(testUser);

        // token firmado con "other", no debería validar con el provider original
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }


}
