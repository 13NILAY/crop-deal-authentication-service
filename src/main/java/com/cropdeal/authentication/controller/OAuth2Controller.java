package com.cropdeal.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class OAuth2Controller {

    @GetMapping("/oauth2-success")
    public ResponseEntity<Map<String, String>> oauth2Success(
            @RequestParam String token,
            @RequestParam String email,
            @RequestParam String role) {
        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "role", role
        ));
    }
}
