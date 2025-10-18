package com.planifAI.auth_service.security;

import com.planifAI.auth_service.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key jwtSecretKey;
    private final String issuer;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:default-secret-key-change-me}") String jwtSecret,
            @Value("${jwt.issuer:planifai}") String issuer,
            @Value("${jwt.access-token-expiration-sec:900}") long accessTokenExpSec,
            @Value("${jwt.refresh-token-expiration-sec:604800}") long refreshTokenExpSec
    ) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.issuer = issuer;
        this.accessTokenExpirationMs = accessTokenExpSec * 1000;
        this.refreshTokenExpirationMs = refreshTokenExpSec * 1000;
    }

    /**
     * Genera un Access Token JWT para un usuario.
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un Refresh Token JWT para un usuario.
     */
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("type", "refresh")
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID getUserIdFromToken(String token) {
        try {
            return UUID.fromString(getClaims(token).getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> getTokenInfo(String token) {
        Claims claims = getClaims(token);
        return Map.of(
                "subject", claims.getSubject(),
                "username", claims.get("username"),
                "roles", claims.get("roles"),
                "issuedAt", claims.getIssuedAt(),
                "expiresAt", claims.getExpiration()
        );
    }

    /**
     * Devuelve la duración (en segundos) de cada tipo de token.
     */
    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpirySeconds() {
        return refreshTokenExpirationMs / 1000;
    }
}
