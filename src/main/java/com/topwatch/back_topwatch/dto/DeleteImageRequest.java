package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteImageRequest(@NotBlank(message = "Url is required") String url) {}