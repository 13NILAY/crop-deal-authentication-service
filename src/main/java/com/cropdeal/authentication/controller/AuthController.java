package com.cropdeal.authentication.controller;

import com.cropdeal.authentication.dto.RegisterRequest;
import com.cropdeal.authentication.dto.LoginRequest;
import com.cropdeal.authentication.dto.AuthResponse;
import com.cropdeal.authentication.dto.UserResponse;
import com.cropdeal.authentication.model.User;
import com.cropdeal.authentication.service.AuthenticationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthenticationService authenticationService;

	public AuthController(AuthenticationService authenticationService) {
		super();
		this.authenticationService = authenticationService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(
			@Valid @RequestBody RegisterRequest request){
		User user=authenticationService.register(request);
		
				return ResponseEntity.status(201).body(UserResponse.from(user));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request ){
				return ResponseEntity.ok(authenticationService.login(request));
	}

	@PostMapping("/roles")
	public ResponseEntity<UserResponse> addRole(@RequestParam String role, java.security.Principal principal) {
		return ResponseEntity.ok(UserResponse.from(authenticationService.addRole(principal.getName(), role)));
	}
	
	
}
