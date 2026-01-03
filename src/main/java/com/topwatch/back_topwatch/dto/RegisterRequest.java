package com.topwatch.back_topwatch.dto;

public record RegisterRequest(
        String email,
        String password,
        String name
) {
}
