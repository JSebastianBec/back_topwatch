package com.topwatch.back_topwatch.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
