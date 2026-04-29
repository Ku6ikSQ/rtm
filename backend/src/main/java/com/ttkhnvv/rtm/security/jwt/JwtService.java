package com.ttkhnvv.rtm.security.jwt;

import com.ttkhnvv.rtm.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JwtService {
    public String generateAccessToken(User user) {
        return "";
    }

    public String generateRefreshToken(User user) {
        return "";
    }

    public String extractEmail(String token) {
        return "";
    }

    public UUID extractUserId(String token) {
        return null;
    }

    public boolean isTokenValid(String token) {
        return true;
    }

    public boolean isTokenExpired(String token) {
        return true;
    }
}
