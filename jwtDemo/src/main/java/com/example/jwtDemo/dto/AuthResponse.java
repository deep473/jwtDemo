package com.example.jwtDemo.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role
) {
}