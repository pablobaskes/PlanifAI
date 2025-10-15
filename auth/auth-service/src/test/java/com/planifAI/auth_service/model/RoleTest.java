package com.planifAI.auth_service.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void shouldBuildRoleCorrectly() {
        Role role = Role.builder()
                .id(1)
                .name("ROLE_USER")
                .build();

        assertThat(role.getId()).isEqualTo(1);
        assertThat(role.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldAllowModificationThroughSetters() {
        Role role = new Role();
        role.setId(2);
        role.setName("ROLE_ADMIN");

        assertThat(role.getId()).isEqualTo(2);
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
    }
}
