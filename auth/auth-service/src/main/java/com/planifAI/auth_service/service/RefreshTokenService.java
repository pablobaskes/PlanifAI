package com.planifAI.auth_service.service;

import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Crea un nuevo refresh token aleatorio y lo almacena en DB (hash).
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, long expirySeconds) {
        // Generar token aleatorio seguro
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);
        Instant now = Instant.now();
        Instant expiry = now.plus(expirySeconds, ChronoUnit.SECONDS);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .issuedAt(now)
                .expiresAt(expiry)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        refreshToken.setTokenHash(rawToken);
        return refreshToken;
    }

    /**
     * Valida si un refresh token existe y no ha expirado ni está revocado.
     */
    public Optional<RefreshToken> validateAndGet(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()));
    }

    /**
     * Revoca un refresh token (p.ej. logout o rotación).
     */
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Revoca todos los refresh tokens del usuario (logout global).
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.findByUser(user).forEach(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Hash del token con SHA-256 para evitar guardar tokens en claro.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Error hashing token", e);
        }
    }
}
