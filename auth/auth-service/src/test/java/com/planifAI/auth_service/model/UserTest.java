package com.planifAI.auth_service.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldBuildUserWithBuilder() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        Role role = Role.builder().id(1).name("ROLE_USER").build();

        User user = User.builder()
                .id(id)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .enabled(true)
                .createdAt(now)
                .roles(Set.of(role))
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void prePersistShouldSetCreatedAt() {
        User user = new User();
        assertThat(user.getCreatedAt()).isNull();

        user.prePersist();

        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void preUpdateShouldSetUpdatedAt() {
        User user = new User();
        assertThat(user.getUpdatedAt()).isNull();

        user.preUpdate();

        assertThat(user.getUpdatedAt()).isNotNull();
    }
}
