package com.cropdeal.authentication.dto;

import com.cropdeal.authentication.model.User;

public record AdminUserResponse(Long id, String email, String role, boolean active) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive());
    }
}