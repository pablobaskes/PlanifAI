package com.planifAI.auth_service.service;

import com.planifAI.auth_service.model.Role;
import com.planifAI.auth_service.model.User;
import com.planifAI.auth_service.repository.RoleRepository;
import com.planifAI.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "1234";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(Role.builder().id(1).name("ROLE_USER").build()));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User savedUser = userService.registerUser(username, email, password);

        // then
        assertThat(savedUser.getUsername()).isEqualTo(username);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionIfUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.registerUser("existing", "x@example.com", "1234")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void shouldThrowExceptionIfEmailExists() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.registerUser("user", "existing@example.com", "1234")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void shouldThrowIfDefaultRoleNotFound() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.registerUser("user", "user@example.com", "1234")
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Default role ROLE_USER not found");
    }
}
