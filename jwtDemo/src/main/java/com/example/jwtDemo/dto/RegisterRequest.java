package com.example.jwtDemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password
) {
}