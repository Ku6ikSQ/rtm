package com.ttkhnvv.rtm.security.jwt;

import com.ttkhnvv.rtm.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final String secretKeyStr;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlDays;
    private final Clock clock;

    @Autowired
    public JwtService(
            @Value("${jwt.secret-key}") String secretKeyStr,
            @Value("${jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
            @Value("${jwt.refresh-token-ttl-days}") long refreshTokenTtlDays,
            Clock clock
    ) {
        this.secretKeyStr = secretKeyStr;
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
        this.clock = clock;
    }

    JwtService(String secretKeyStr, long accessTokenTtlMinutes, long refreshTokenTtlDays) {
        this(secretKeyStr, accessTokenTtlMinutes, refreshTokenTtlDays, Clock.systemUTC());
    }

    public String generateAccessToken(User user) {
        var now = clock.millis();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 1000 * 60 * accessTokenTtlMinutes))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        var now = clock.millis();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 1000L * 60 * 60 * 24 * refreshTokenTtlDays))
                .signWith(getSecretKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(Date.from(clock.instant()));
        } catch (JwtException e) {
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyStr));
    }
}
