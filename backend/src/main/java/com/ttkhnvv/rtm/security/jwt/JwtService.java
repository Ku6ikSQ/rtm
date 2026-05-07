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

/**
 * Handles JWT token generation, validation and claims extraction.
 * Uses HMAC-SHA256 signing algorithm.
 */
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

    /**
     * Generates a short-lived access token for the given user.
     * Contains email, role and userId claims.
     *
     * @param user the user to generate the token for
     * @return signed JWT access token
     */
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

    /**
     * Generates a long-lived refresh token for the given user.
     * Contains email and userId claims.
     *
     * @param user the user to generate the token for
     * @return signed JWT refresh token
     */
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

    /**
     * Extracts the email address from the token's subject claim.
     *
     * @param token the JWT token
     * @return the email address
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the user ID from the token's userId claim.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    /**
     * Validates the token's signature and expiration.
     *
     * @param token the JWT token to validate
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
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

    /**
     * Validates the token's signature, expiration and verifies
     * that the token belongs to the given user.
     *
     * @param token the JWT token to validate
     * @param userDetails the user to validate the token against
     * @return {@code true} if the token is valid and belongs to the user, {@code false} otherwise
     */
    // TODO: remove it
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Checks whether the token has expired.
     *
     * @param token the JWT token to check
     * @return {@code true} if the token is expired or invalid, {@code false} otherwise
     */
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
