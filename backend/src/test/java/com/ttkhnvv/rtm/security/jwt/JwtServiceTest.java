package com.ttkhnvv.rtm.security.jwt;


import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private static final String SECRET_KEY
            = "9clnLqt2iaXR082zDhflEkWGKdJpxZEfzWpRPRBazIc=";
    private static final int ACCESS_TOKEN_TTL = 15;
    private static final int REFRESH_TOKEN_TTL = 7;

    private JwtService jwtService;
    private User user;
    private String invalidToken;

    @Mock
    private UserDetails userDetails;


    @BeforeEach
    void setup() {
        jwtService = new JwtService(SECRET_KEY, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL);
        user = User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("user@email.com")
                .role(UserRole.USER)
                .passwordHash("password")
                .build();
        invalidToken =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NT";
    }

    @Test
    void generateAccessToken_shouldContainEmail() {
        // given

        // when
        var token = jwtService.generateAccessToken(user);

        // then
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@email.com");
    }

    @Test
    void generateRefreshToken_shouldContainEmail() {
        // given

        // when
        var token = jwtService.generateRefreshToken(user);

        // then
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@email.com");
    }

    @Test
    void generateAccessToken_shouldContainUserId() {
        // when
        var token = jwtService.generateAccessToken(user);

        // then
        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        // given
        var token = jwtService.generateRefreshToken(user);

        // when
        var isValid = jwtService.isTokenValid(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsInvalid() {
        // given
        var token = invalidToken;

        // when
        var isValid = jwtService.isTokenValid(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnTrue_whenTokenIsExpired() {
        // given
        var token = jwtService.generateAccessToken(user);

        // when
        var futureClock = Clock.fixed(
                Instant.now().plus(1, ChronoUnit.HOURS),
                ZoneOffset.UTC
        );
        var futureJwtService = new JwtService(SECRET_KEY, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL, futureClock);

        // then
        assertThat(futureJwtService.isTokenExpired(token)).isTrue();
    }

    @Test
    void isTokenExpired_shouldReturnFalse_whenTokenIsUnexpired() {
        // when
        var token = jwtService.generateAccessToken(user);

        // then
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenEmailDoesNotMatch() {
        // given
        var token = jwtService.generateAccessToken(user);
        when(userDetails.getUsername()).thenReturn("new" + user.getEmail());

        // when
        var isValid = jwtService.isTokenValid(token, userDetails);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenEmailMatches() {
        // given
        var token = jwtService.generateAccessToken(user);
        when(userDetails.getUsername()).thenReturn(user.getEmail());

        // when
        var isValid = jwtService.isTokenValid(token, userDetails);

        // then
        assertThat(isValid).isTrue();
    }
}
