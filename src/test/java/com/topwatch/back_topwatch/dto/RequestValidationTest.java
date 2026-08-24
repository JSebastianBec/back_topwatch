package com.topwatch.back_topwatch.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void registerRequest_isInvalid_whenEmailIsBlank() {
        RegisterRequest request = new RegisterRequest("", "password123", "sebas");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerRequest_isInvalid_whenEmailIsMalformed() {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123", "sebas");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerRequest_isInvalid_whenPasswordIsTooShort() {
        RegisterRequest request = new RegisterRequest("sebas@topwatch.com", "short", "sebas");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void registerRequest_isInvalid_whenNicknameIsBlank() {
        RegisterRequest request = new RegisterRequest("sebas@topwatch.com", "password123", "  ");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nickname"));
    }

    @Test
    void registerRequest_isValid_whenAllFieldsAreCorrect() {
        RegisterRequest request = new RegisterRequest("sebas@topwatch.com", "password123", "sebas");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_isInvalid_whenFieldsAreBlank() {
        LoginRequest request = new LoginRequest("", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void loginRequest_isValid_whenFieldsAreProvided() {
        LoginRequest request = new LoginRequest("sebas@topwatch.com", "password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateUserRequest_isValid_whenAllFieldsAreNull() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, null);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateUserRequest_isInvalid_whenEmailIsMalformed() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "not-an-email", null, null);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void updateUserRequest_isInvalid_whenPasswordIsTooShort() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, "short", null);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
