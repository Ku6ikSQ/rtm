package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.auth.*;
import com.ttkhnvv.rtm.entity.User;
import com.ttkhnvv.rtm.entity.UserRole;
import com.ttkhnvv.rtm.exception.auth.*;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

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

    public void logout(RefreshRequest request) {
        var refresh = request.getRefreshToken();

        if (!jwtService.isTokenValid(refresh))
            throw new InvalidTokenException("Invalid refresh token were taken.");

        var userId = jwtService.extractUserId(refresh);
        tokenRepository.delete(userId);
    }

    public boolean isUserExistsByUsername(String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

    public boolean isUserExistsByEmail(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }
}
