package com.cropdeal.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminActivityRequest(@NotBlank String action,
                                   @NotBlank String entityType,
                                   @NotBlank String entityId) {
}