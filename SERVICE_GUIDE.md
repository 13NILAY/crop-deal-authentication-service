# Authentication Service Guide

## Purpose
Authentication Service owns user identity, passwords, roles, registration, login, and JWT creation. It does not own farmer, dealer, crop, order, or payment data.

## Runtime
- Port: `8081`
- Database: `cropdeal_auth`
- Main API prefix: `/auth`
- Authentication: Spring Security and JWT

## Main flow
1. `POST /auth/register` validates email/password and creates a user.
2. Passwords are hashed with BCrypt before storage.
3. `POST /auth/login` verifies the password.
4. JwtService creates a signed token.
5. The token subject is the user's email.
6. The token contains the user's roles.
7. Other services validate the same token using the shared JWT secret.

## Roles
A user can have multiple roles, such as `FARMER,DEALER`. `POST /auth/roles?role=FARMER` adds a role for the authenticated user. Login again after changing roles to receive a new token.

## Important files
- `model/User`: user identity and role storage
- `repository/UserRepository`: user persistence
- `service/AuthenticationService`: registration/login logic
- `service/JwtService`: token generation and validation
- `security/JwtAuthenticationFilter`: authentication filter
- `controller/AuthController`: HTTP API

## Security boundary
This service authenticates users. Farmer, Dealer, Order, Payment, and other services still authorize requests locally based on JWT roles and ownership.
