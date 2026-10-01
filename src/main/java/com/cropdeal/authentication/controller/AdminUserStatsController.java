package com.cropdeal.authentication.controller;

import com.cropdeal.authentication.repository.UserRepository;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserStatsController {
    private final UserRepository repository;

    public AdminUserStatsController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        return Map.of("totalUsers", repository.count(),
                "farmers", repository.countUsersWithRole("FARMER"),
                "dealers", repository.countUsersWithRole("DEALER"),
                "admins", repository.countUsersWithRole("ADMIN"),
                "activeUsers", repository.countByActive(true),
                "inactiveUsers", repository.countByActive(false));
    }
}