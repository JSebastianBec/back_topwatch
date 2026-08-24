package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    String name,
    String lastname,
    String nickname,
    Gender gender,
    @Email(message = "Email must be valid")
    String email,
    String avatarURL
) {

}
