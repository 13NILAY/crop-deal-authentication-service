package com.cropdeal.authentication.dto;

import com.cropdeal.authentication.model.User;

public record UserResponse(Long id, String email, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }
}