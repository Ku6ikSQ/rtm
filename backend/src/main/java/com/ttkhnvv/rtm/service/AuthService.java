package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.auth.*;
import com.ttkhnvv.rtm.entity.User;
import com.ttkhnvv.rtm.repository.UserRepository;
import com.ttkhnvv.rtm.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        return null;
    }

    public AuthResponse login(LoginRequest request) {
        return null;
    }

    public RefreshResponse refresh(RefreshRequest request) {
        return null;
    }

    public void logout(RefreshRequest request) {
    }
}
