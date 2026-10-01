package com.cropdeal.authentication.service;

import com.cropdeal.authentication.dto.LoginRequest;
import com.cropdeal.authentication.service.JwtService;
import com.cropdeal.authentication.dto.RegisterRequest;
import com.cropdeal.authentication.dto.AuthResponse;
import com.cropdeal.authentication.model.User;
import com.cropdeal.authentication.repository.UserRepository;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import com.cropdeal.authentication.dto.AdminUserResponse;

@Service
public class AuthenticationService {
	private static final Set<String> SELF_SERVICE_ROLES = Set.of("DEALER", "FARMER");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	

	public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService=jwtService;
	}
	
	
	public User register(RegisterRequest request) {
		String role = request.getRole() == null || request.getRole().isBlank()
				? "DEALER" : request.getRole().trim().toUpperCase(Locale.ROOT);
		if (!SELF_SERVICE_ROLES.contains(role)) {
			throw new IllegalArgumentException("Unsupported role");
		}

		if(userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		
		User user=new User();
		
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(role);
		
		return userRepository.save(user);
	}
	
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail()).orElse(null);
		if (user == null || !user.isActive() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadCredentialsException("Invalid email or password");
		}
		
				return new AuthResponse(jwtService.generateToken(user.getEmail(),user.getRole()),
								com.cropdeal.authentication.dto.UserResponse.from(user));
		
	}

	public User addRole(String email, String role) {
		String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
		if (!SELF_SERVICE_ROLES.contains(normalized)) {
			throw new IllegalArgumentException("Unsupported role");
		}

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		java.util.Set<String> roles = new java.util.LinkedHashSet<>(java.util.Arrays.asList(user.getRole().split(",")));
		roles.add(normalized);
		user.setRole(String.join(",", roles));
		return userRepository.save(user);
	}

	public Page<AdminUserResponse> searchUsers(String search, String role, Pageable pageable) {
		String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
		String normalizedRole = role == null || role.isBlank() ? null : role.trim().toUpperCase(Locale.ROOT);
		if (normalizedRole != null && !Set.of("DEALER", "FARMER", "ADMIN").contains(normalizedRole)) {
			throw new IllegalArgumentException("Unsupported role");
		}

		Specification<User> specification = (root, query, criteriaBuilder) -> {
			var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
			if (!normalizedSearch.isEmpty()) {
				predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
						"%" + normalizedSearch + "%"));
			}
			if (normalizedRole != null) {
				var storedRoles = criteriaBuilder.concat(
						criteriaBuilder.concat(",", criteriaBuilder.upper(root.get("role"))), ",");
				predicates.add(criteriaBuilder.like(storedRoles, "%," + normalizedRole + ",%"));
			}
			return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		};

		Pageable boundedPageable = PageRequest.of(pageable.getPageNumber(),
				Math.min(pageable.getPageSize(), 100), pageable.getSort());
		return userRepository.findAll(specification, boundedPageable).map(AdminUserResponse::from);
	}

	public AdminUserResponse getUser(Long id) {
		return userRepository.findById(id).map(AdminUserResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public AdminUserResponse updateUserStatus(Long id, boolean active, String adminEmail) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		if (!active && user.getEmail().equalsIgnoreCase(adminEmail)) {
			throw new AccessDeniedException("Admins cannot deactivate their own account");
		}
		user.setActive(active);
		return AdminUserResponse.from(userRepository.save(user));
	}
	
}
