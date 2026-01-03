package com.topwatch.back_topwatch.dto;

public record LoginRequest(
        String email,
        String password
) {
}
