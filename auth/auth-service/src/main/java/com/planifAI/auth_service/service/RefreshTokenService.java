package com.planifAI.auth_service.service;

import com.planifAI.auth_service.dto.RefreshTokenWithRawToken;
import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
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
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenExpirySeconds;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration-sec:604800}")
            long refreshTokenExpirySeconds) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    /**
     * Crea un nuevo refresh token aleatorio y lo almacena en DB (hash).
     */
    @Transactional
    public String createRefreshToken(User user, long expirySeconds) {
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

        return rawToken;
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
     * Valida, revoca el token antiguo, crea y guarda uno nuevo, y devuelve el nuevo token.
     * * @param oldRefreshTokenRaw El Refresh Token original enviado por el cliente.
     * @return Optional con el nuevo RefreshToken (que contiene el raw token y el usuario).
     */
    @Transactional
    public Optional<RefreshTokenWithRawToken> validateAndRevokeAndGetNew(String oldRefreshTokenRaw) {
        String tokenHash = hashToken(oldRefreshTokenRaw);

        // 1. Encontrar y validar el token antiguo
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .map(oldRt -> {
                    // 2. Revocar el token antiguo
                    oldRt.setRevoked(true);
                    refreshTokenRepository.save(oldRt); // Guardar la revocación

                    // 3. Generar un nuevo Refresh Token (¡rotación!)
                    User user = oldRt.getUser();

                    // Generar token RAW y guardarlo
                    String newRawToken = createRefreshToken(user, refreshTokenExpirySeconds);

                    // Creamos un objeto que contenga el User y el nuevo Raw Token
                    return new RefreshTokenWithRawToken(user, newRawToken);
                });
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
