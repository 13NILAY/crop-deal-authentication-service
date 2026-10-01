package com.cropdeal.authentication.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cropdeal.authentication.dto.RegisterRequest;
import com.cropdeal.authentication.model.User;
import com.cropdeal.authentication.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthenticationService service;

    @Test
    void registerHashesPasswordAndUsesDealerDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dealer@example.com");
        request.setPassword("secret123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.register(request);

        assertEquals("dealer@example.com", saved.getEmail());
        assertEquals("hashed", saved.getPassword());
        assertEquals("DEALER", saved.getRole());
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("secret123");
        request.setRole("ADMIN");

        assertThrows(IllegalArgumentException.class, () -> service.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRoleKeepsExistingRoleAndAddsFarmerRole() {
        User user = new User("person@example.com", "hashed", "DEALER");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = service.addRole(user.getEmail(), "farmer");

        assertEquals("DEALER,FARMER", updated.getRole());
    }

    @Test
    void addRoleRejectsAdminSelfAssignment() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addRole("person@example.com", "ADMIN"));
        verify(userRepository, never()).save(any(User.class));
    }
}