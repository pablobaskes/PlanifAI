package com.planifai.core.common.exception;

import com.planifai.core.finance.domain.exception.SavingsGoalNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequestMapsIllegalArgumentExceptionTo400() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBadRequest(
                new IllegalArgumentException("Invalid request.")
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid request.", response.getBody().message());
    }

    @Test
    void handleNotFoundMapsDomainNotFoundExceptionTo404() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNotFound(
                new SavingsGoalNotFoundException(42L)
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Savings goal not found: 42", response.getBody().message());
    }
}
