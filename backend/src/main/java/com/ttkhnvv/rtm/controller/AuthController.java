package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.auth.*;
import com.ttkhnvv.rtm.exception.auth.*;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleUser;
import com.ttkhnvv.rtm.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

/**
 * Handles authentication operations: registration, login, token refresh and logout.
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping(API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Registers a new user and returns access and refresh tokens.
     *
     * @param request registration data (username, email and password)
     * @return access and refresh tokens
     * @throws EmailAlreadyTakenException    if the provided email is already registered
     * @throws UsernameAlreadyTakenException if the provided username is already taken
     */
    @SecurityRequirements
    @HasRoleAny
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    /**
     * Authenticates a user and returns access and refresh tokens.
     *
     * @param request login data (email and password)
     * @return access and refresh tokens
     * @throws UserNotFoundException    if no user was found with the provided email
     * @throws UserInactiveException    if the user account has been blocked
     * @throws InvalidPasswordException if the provided password is incorrect
     */
    @SecurityRequirements
    @HasRoleAny
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(200).body(authService.login(request));
    }

    /**
     * Validates the refresh token and issues a new access and refresh token pair.
     *
     * @param request refresh data (refresh token)
     * @return new access and refresh tokens
     * @throws InvalidTokenException if the provided refresh token is invalid or expired
     * @throws UserNotFoundException if no user was found associated with the token
     */
    @HasRoleAny
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.status(200).body(authService.refresh(request));
    }

    /**
     * Invalidates the refresh token and terminates the user session.
     *
     * @param request logout data (refresh token)
     * @throws InvalidTokenException if the provided refresh token is invalid or expired
     */
    @HasRoleAny
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}