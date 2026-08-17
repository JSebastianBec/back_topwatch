package com.topwatch.back_topwatch.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDuplicateResource_returnsConflictWithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDuplicateResource(new DuplicateResourceException("Nickname is already in use"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Nickname is already in use");
    }

    @Test
    void handleIllegalArgument_returnsUnauthorizedWithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid refresh token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "Invalid refresh token");
    }

    @Test
    void handleDataIntegrityViolation_returnsConflictWithGenericMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint violation"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Email or nickname is already in use");
    }
}
