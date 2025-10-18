package com.planifAI.auth_service.integration;

import com.jayway.jsonpath.JsonPath;
import com.planifAI.auth_service.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
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

    @Test
    @Order(2)
    void shouldRefreshAccessTokenSuccessfully() {
        // 1️⃣ Registrar un usuario
        RegisterRequest register = new RegisterRequest("refresher", "ref@example.com", "Pass1234!");
        restTemplate.postForEntity(baseUrl() + "/register", register, Map.class);

        // 2️⃣ Hacer login para obtener tokens
        Map<String, String> loginReq = Map.of(
                "usernameOrEmail", "refresher",
                "password", "Pass1234!"
        );

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity(baseUrl() + "/login", loginReq, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKeys("accessToken", "refreshToken");

        // Almacenamos los tokens del primer login
        String loginAccessToken = (String) loginResponse.getBody().get("accessToken");
        String refreshToken = (String) loginResponse.getBody().get("refreshToken");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test Thread interrupted", e);
        }

        // 3️⃣ Llamar al endpoint /refresh
        Map<String, String> refreshBody = Map.of("refreshToken", refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(refreshBody, headers);

        ResponseEntity<Map> refreshResponse =
                restTemplate.exchange(
                        baseUrl() + "/refresh",
                        HttpMethod.POST,
                        requestEntity,
                        Map.class
                );

        // 4️⃣ Validar respuesta
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).containsKeys("accessToken", "refreshToken", "expiresIn");

        // Comparamos el nuevo Access Token con el Access Token original almacenado
        assertThat(refreshResponse.getBody().get("accessToken")).isNotEqualTo(loginAccessToken);
    }

    @Test
    @Order(3)
    void shouldRejectRegistrationWithExistingEmail() {
        // 1️⃣ Registro inicial
        RegisterRequest request1 = new RegisterRequest("userdup", "dup@example.com", "Pass1234!");
        ResponseEntity<Map> response1 =
                restTemplate.postForEntity(baseUrl() + "/register", request1, Map.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2️⃣ Intento de registro con el mismo email
        RegisterRequest request2 = new RegisterRequest("anotheruser", "dup@example.com", "Pass1234!");
        ResponseEntity<String> response2 =
                restTemplate.postForEntity(baseUrl() + "/register", request2, String.class);

        // 3️⃣ Validación
        assertThat(response2.getStatusCode().value())
                .isIn(HttpStatus.BAD_REQUEST.value(), HttpStatus.CONFLICT.value());
        assertThat(response2.getBody()).contains("Email already in use");
    }

    @Test
    @Order(4)
    void shouldFailLoginWithIncorrectPassword() {
        // 1️⃣ Registramos un usuario válido
        RegisterRequest register = new RegisterRequest("wrongpass", "wrong@example.com", "Correct123!");
        restTemplate.postForEntity(baseUrl() + "/register", register, Map.class);

        // 2️⃣ Intentamos hacer login con contraseña incorrecta
        Map<String, String> badLogin = Map.of(
                "usernameOrEmail", "wrongpass",
                "password", "WrongPassword!"
        );

        ResponseEntity<String> response =
                restTemplate.postForEntity(baseUrl() + "/login", badLogin, String.class);

        // 3️⃣ Validamos que responde 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid credentials");
    }

    @Test
    @Order(5)
    void shouldFailRefreshWithInvalidToken() {
        // 1️⃣ Enviamos un refresh token inventado
        Map<String, String> refreshReq = Map.of("refreshToken", "invalid-or-expired-token-123");

        ResponseEntity<String> response =
                restTemplate.postForEntity(baseUrl() + "/refresh", refreshReq, String.class);

        // 2️⃣ Validamos la respuesta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Test principal: Se loguea, se desloguea y verifica que el token esté revocado.
     */
    @Test
    @Order(6)
    void shouldLogoutSuccessfullyAndRevokeToken() {
        // 1️⃣ Registrar un usuario. Usamos Void.class ya que /register típicamente devuelve 201 sin cuerpo complejo.
        RegisterRequest register = new RegisterRequest("logouter", "logout@example.com", "Pass1234!");
        restTemplate.postForEntity(baseUrl() + "/register", register, Void.class);

        // 2️⃣ Hacer login para obtener tokens
        Map<String, String> loginReq = Map.of(
                "usernameOrEmail", "logouter",
                "password", "Pass1234!"
        );

        ResponseEntity<String> loginResponse =
                restTemplate.postForEntity(baseUrl() + "/login", loginReq, String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String refreshToken = JsonPath.read(loginResponse.getBody(), "$.refreshToken");

        // 3️⃣ Llamar al endpoint /logout
        Map<String, String> logoutBody = Map.of("refreshToken", refreshToken);

        ResponseEntity<String> logoutResponse =
                restTemplate.postForEntity(baseUrl() + "/logout", logoutBody, String.class);

        // 4️⃣ Validar que el logout fue exitoso
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String logoutMessage = JsonPath.read(logoutResponse.getBody(), "$.message");
        assertThat(logoutMessage).isEqualTo("Logged out successfully");
        // 5️⃣ CASO NEGATIVO: Intentar usar el Refresh Token revocado
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(logoutBody, headers);

        // 💡 CAMBIO: Usar String.class
        ResponseEntity<String> refreshAfterLogoutResponse =
                restTemplate.exchange(
                        baseUrl() + "/refresh",
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

        // El servicio de refresh debería devolver 401 si el token no se encuentra/es inválido
        assertThat(refreshAfterLogoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Verifica que intentar desloguearse con un token ya revocado no cause un error.
     */
    @Test
    @Order(7)
    void shouldFailToLogoutWithRevokedToken() {
        // 2️⃣ Registrar y loguear un nuevo usuario
        RegisterRequest register = new RegisterRequest("revoked", "revoked@example.com", "Pass1234!");
        restTemplate.postForEntity(baseUrl() + "/register", register, Void.class);

        Map<String, String> loginReq = Map.of("usernameOrEmail", "revoked", "password", "Pass1234!");

        ResponseEntity<String> loginResponse =
                restTemplate.postForEntity(baseUrl() + "/login", loginReq, String.class);

        String refreshToken = JsonPath.read(loginResponse.getBody(), "$.refreshToken");
        Map<String, String> logoutBody = Map.of("refreshToken", refreshToken);

        // 3️⃣ Primer Logout (revocación exitosa)
        restTemplate.postForEntity(baseUrl() + "/logout", logoutBody, String.class);

        // 4️⃣ Segundo Logout (Debe devolver OK si el servicio ignora el re-revoke)
        ResponseEntity<String> secondLogoutResponse =
                restTemplate.postForEntity(baseUrl() + "/logout", logoutBody, String.class);

        // El resultado debe ser 200 OK.
        assertThat(secondLogoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Verifica la validación a nivel de Controller para tokens faltantes o vacíos.
     */
    @Test
    @Order(8)
    void shouldReturnBadRequestIfRefreshTokenIsMissing() {
        // 1️⃣ Intentar Logout con token nulo (Map vacío)
        Map<String, String> missingBody = Map.of();

        ResponseEntity<String> missingResponse =
                restTemplate.postForEntity(baseUrl() + "/logout", missingBody, String.class);

        assertThat(missingResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String missingMessageNull = JsonPath.read(missingResponse.getBody(), "$.message");
        assertThat(missingMessageNull).isEqualTo("Missing refresh token");

        // 2️⃣ Intentar Logout con token vacío
        Map<String, String> emptyBody = Map.of("refreshToken", "");

        ResponseEntity<String> emptyResponse =
                restTemplate.postForEntity(baseUrl() + "/logout", emptyBody, String.class);

        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String missingMessage = JsonPath.read(missingResponse.getBody(), "$.message");
        assertThat(missingMessage).isEqualTo("Missing refresh token");    }

    /**
     * Verifica que intentar desloguearse con un token inexistente termine con éxito.
     */
    @Test
    @Order(9)
    void shouldReturnOkForNonExistentToken() {
        // 1️⃣ Token que no existe en la base de datos (nunca se emitió)
        String nonExistentToken = "non-existent-random-token-string-12345";
        Map<String, String> logoutBody = Map.of("refreshToken", nonExistentToken);

        ResponseEntity<String> response =
                restTemplate.postForEntity(baseUrl() + "/logout", logoutBody, String.class);

        // El servicio devuelve 200 OK porque el token no existía, por lo que el usuario está "deslogueado".
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String logoutSuccessMessage = JsonPath.read(response.getBody(), "$.message");
        assertThat(logoutSuccessMessage).isEqualTo("Logged out successfully");    }
}
