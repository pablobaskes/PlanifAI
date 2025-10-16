package com.planifAI.auth_service.integration;

import com.planifAI.auth_service.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // usamos Flyway
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");

        String secureKey = "c2VjcmV0U3VwZXJTdWJzdGl0dXRlS2V5Rm9ySURvY3Rlc3Q=";

        registry.add("jwt.secret", () -> secureKey);
        registry.add("jwt.access-token-expiration-sec", () -> "900");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @BeforeEach
    void beforeEach() {
        // limpia tablas si existen (si no existen, ignora)
        try { jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens CASCADE"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("TRUNCATE TABLE user_roles CASCADE"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("TRUNCATE TABLE users CASCADE"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("TRUNCATE TABLE roles CASCADE"); } catch (Exception ignored) {}
    }

    @Test
    @Order(1)
    void registerAndLoginFlow() {
        // 1) register
        RegisterRequest register = new RegisterRequest("intuser", "int@example.com", "Password123!");
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(baseUrl() + "/register", register, Map.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map body = registerResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isNotNull();

        // Verificar usuario en BD
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ? AND email = ?",
                Integer.class,
                "intuser", "int@example.com"
        );
        assertThat(count).isEqualTo(1);

        // 2) login
        Map<String, String> loginReq = Map.of("usernameOrEmail", "intuser", "password", "Password123!");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl() + "/login", loginReq, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map loginBody = loginResponse.getBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.get("accessToken")).isNotNull();
        assertThat(loginBody.get("refreshToken")).isNotNull();
    }
}
