package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.auth.*;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import com.ttkhnvv.rtm.exception.auth.*;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles authentication business logic: registration, login, token refresh and logout.
 * Manages JWT token lifecycle and user session persistence in token store.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    /**
     * Registers a new user and saves the refresh token to token store.
     *
     * @param request registration data (username, email and password)
     * @return access and refresh tokens
     * @throws EmailAlreadyTakenException if the provided email is already registered
     * @throws UsernameAlreadyTakenException if the provided username is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        if (isUserExistsByEmail(request.getEmail()))
            throw new EmailAlreadyTakenException("This email already taken.");
        if (isUserExistsByUsername(request.getUsername()))
            throw new UsernameAlreadyTakenException("This username already taken.");

        var user =
                userRepository.save(
                        User
                                .builder()
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(UserRole.USER)
                                .isActive(true)
                                .build()
                );

        var refresh = jwtService.generateRefreshToken(user);
        var access = jwtService.generateAccessToken(user);
        tokenRepository.save(user.getId(), refresh);

        return AuthResponse
                .builder()
                .refreshToken(refresh)
                .accessToken(access)
                .build();
    }


    /**
     * Validates user credentials and generates a new token pair.
     * Saves the refresh token to token store for subsequent validation.
     *
     * @param request login data (email and password)
     * @return access and refresh tokens
     * @throws UserNotFoundException if no user was found with the provided email
     * @throws UserInactiveException if the user account has been blocked
     * @throws InvalidPasswordException if the provided password is incorrect
     */
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Failed to find user."));
        if (!user.getIsActive())
            throw new UserInactiveException("Your account has been blocked.");

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new InvalidPasswordException("Invalid password.");

        var refresh = jwtService.generateRefreshToken(user);
        var access = jwtService.generateAccessToken(user);
        tokenRepository.save(user.getId(), refresh);

        return AuthResponse.builder()
                .refreshToken(refresh)
                .accessToken(access)
                .build();
    }


    /**
     * Validates the refresh token, rotates the token pair and updates token store.
     * Implements refresh token rotation — each refresh token is single-use.
     *
     * @param request refresh data (refresh token)
     * @return new access and refresh tokens
     * @throws InvalidTokenException if the provided token is invalid, expired or does not match the stored token
     * @throws UserNotFoundException if no user was found associated with the token
     */
    public RefreshResponse refresh(RefreshRequest request) {
        var refresh = request.getRefreshToken();
        if (!jwtService.isTokenValid(refresh))
            throw new InvalidTokenException("Invalid refresh token were taken.");

        var tokenUserId = jwtService.extractUserId(refresh);
        var savedRefresh = tokenRepository.find(tokenUserId)
                .orElseThrow(() -> new InvalidTokenException("This session has been revoked. Please log in again."));
        if (!savedRefresh.equals(refresh))
            throw new InvalidTokenException("Invalid refresh token were taken.");

        var user = userRepository.findById(tokenUserId).orElseThrow(() ->
                new UserNotFoundException(String.format("Failed to find user with id = %s", tokenUserId)));

        var newRefresh = jwtService.generateRefreshToken(user);
        var newAccess = jwtService.generateAccessToken(user);
        tokenRepository.delete(user.getId());
        tokenRepository.save(user.getId(), newRefresh);

        return RefreshResponse
                .builder()
                .refreshToken(newRefresh)
                .accessToken(newAccess)
                .build();
    }


    /**
     * Validates the refresh token and removes it from token store, terminating the session.
     *
     * @param request logout data (refresh token)
     * @throws InvalidTokenException if the provided refresh token is invalid or expired
     */
    public void logout(RefreshRequest request) {
        var refresh = request.getRefreshToken();

        if (!jwtService.isTokenValid(refresh))
            throw new InvalidTokenException("Invalid refresh token were taken.");

        var userId = jwtService.extractUserId(refresh);
        tokenRepository.delete(userId);
    }

    private boolean isUserExistsByUsername(String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

    private boolean isUserExistsByEmail(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }
}
