package com.cropdeal.authentication.dto;

public record AuthResponse(String token, UserResponse user) {
}