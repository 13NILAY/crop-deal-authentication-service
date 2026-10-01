package com.cropdeal.authentication.service;
 
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
 
import javax.crypto.SecretKey;
 
@Service
public class JwtService {
 
        private final SecretKey secretKey;

        public JwtService(@Value("${security.jwt.secret}") String secret) {
                this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
 
        public String generateToken(String email, String role) {
 
        return Jwts.builder()
                .subject(email)
                .claim("roles", role)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(
                        System.currentTimeMillis() + 1000 * 60 * 60
                ))
                .signWith(secretKey)
                .compact();
    }

        public String extractEmail(String token) {
                return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                                .getPayload().getSubject();
        }

        public boolean isValid(String token, String email) {
                try {
                        return email.equals(extractEmail(token));
                } catch (RuntimeException ex) {
                        return false;
                }
        }

        public boolean isTokenValid(String token) {
                try {
                        extractEmail(token);
                        return true;
                } catch (RuntimeException ex) {
                        return false;
                }
        }
}