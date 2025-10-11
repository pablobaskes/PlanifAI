package com.planifAI.auth_service.repository;


import com.planifAI.auth_service.model.RefreshToken;
import com.planifAI.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
}
