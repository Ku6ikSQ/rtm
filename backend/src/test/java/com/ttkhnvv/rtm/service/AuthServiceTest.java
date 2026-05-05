package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.auth.LoginRequest;
import com.ttkhnvv.rtm.dto.auth.RefreshRequest;
import com.ttkhnvv.rtm.dto.auth.RegisterRequest;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import com.ttkhnvv.rtm.exception.auth.*;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void init() {
        user = User
                .builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("user@email.com")
                .passwordHash("p@assw0rd")
                .role(UserRole.USER)
                .isActive(true)
                .build();
    }

    @Nested
    class Register {
        @Test
        void shouldReturnValidAuthResponse_whenNoSuchUser() {
            // given
            var req =
                    RegisterRequest
                            .builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);
            when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_password");

            // when
            var res = authService.register(req);

            // then
            assertThat(res.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(res.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        void shouldThrowException_whenUsernameAlreadyTaken() {
            // given
            var req =
                    RegisterRequest
                            .builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(UsernameAlreadyTakenException.class);
        }

        @Test
        void shouldThrowException_whenEmailAlreadyTaken() {
            // given
            var req =
                    RegisterRequest
                            .builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

            // when/then
            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(EmailAlreadyTakenException.class);
        }
    }

    @Nested
    class Login {
        @Test
        void shouldReturnValidAuthResponse_whenValidActiveUserFound() {
            // given
            var req =
                    LoginRequest
                            .builder()
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByEmail(req.getEmail()))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
                    .thenReturn(true);
            when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);

            // when
            var res = authService.login(req);

            // then
            assertThat(res.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(res.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        void shouldThrowValidException_whenValidInactiveUserFound() {
            // given
            user.setIsActive(false);
            var req =
                    LoginRequest
                            .builder()
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByEmail(req.getEmail()))
                    .thenReturn(Optional.of(user));

            // when/then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(UserInactiveException.class);
        }

        @Test
        void shouldThrowException_whenInvalidPasswordTaken() {
            // given
            var req =
                    LoginRequest
                            .builder()
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByEmail(req.getEmail()))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
                    .thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            var req =
                    LoginRequest
                            .builder()
                            .email(user.getEmail())
                            .password(user.getPasswordHash())
                            .build();
            when(userRepository.findUserByEmail(req.getEmail()))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Refresh {
        @Test
        void shouldReturnValidRefreshResponse_whenValidRefreshTokenTaken() {
            // given
            var newRefresh = REFRESH_TOKEN + "_new";
            var newAccess = ACCESS_TOKEN + "_new";
            var req =
                    RefreshRequest
                            .builder()
                            .refreshToken(REFRESH_TOKEN)
                            .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(true);
            when(jwtService.extractUserId(req.getRefreshToken()))
                    .thenReturn(user.getId());
            when(tokenRepository.find(user.getId()))
                    .thenReturn(Optional.of(REFRESH_TOKEN));
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateRefreshToken(user))
                    .thenReturn(newRefresh);
            when(jwtService.generateAccessToken(user))
                    .thenReturn(newAccess);

            // when
            var res = authService.refresh(req);

            // then
            assertThat(res.getAccessToken()).isEqualTo(newAccess);
            assertThat(res.getRefreshToken()).isEqualTo(newRefresh);
        }

        @Test
        void shouldThrowException_whenInvalidRefreshTokenTaken() {
            // given
            var req =
                    RefreshRequest
                            .builder()
                            .refreshToken(REFRESH_TOKEN)
                            .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.refresh(req))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void shouldThrowException_whenRefreshTokenRevoked() {
            // given
            var req =
                    RefreshRequest
                            .builder()
                            .refreshToken(REFRESH_TOKEN)
                            .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(true);
            when(jwtService.extractUserId(req.getRefreshToken()))
                    .thenReturn(user.getId());
            when(tokenRepository.find(user.getId()))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.refresh(req))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void shouldThrowException_whenRefreshTokenDoesNotMatchStored() {
            // given
            var newRefresh = REFRESH_TOKEN + "_new";
            var req =
                    RefreshRequest
                            .builder()
                            .refreshToken(REFRESH_TOKEN)
                            .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(true);
            when(jwtService.extractUserId(req.getRefreshToken()))
                    .thenReturn(user.getId());
            when(tokenRepository.find(user.getId()))
                    .thenReturn(Optional.of(newRefresh));

            // when/then
            assertThatThrownBy(() -> authService.refresh(req))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            var newRefresh = REFRESH_TOKEN + "_new";
            var req =
                    RefreshRequest
                            .builder()
                            .refreshToken(REFRESH_TOKEN)
                            .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(true);
            when(jwtService.extractUserId(req.getRefreshToken()))
                    .thenReturn(user.getId());
            when(tokenRepository.find(user.getId()))
                    .thenReturn(Optional.of(REFRESH_TOKEN));
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.refresh(req))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Logout {
        @Test
        void shouldDeleteTokenFromStore_whenValidTokenTaken() {
            // given
            var req = RefreshRequest
                    .builder()
                    .refreshToken(REFRESH_TOKEN)
                    .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(true);
            when(jwtService.extractUserId(req.getRefreshToken()))
                    .thenReturn(user.getId());

            // when
            authService.logout(req);

            // then
            verify(tokenRepository, times(1)).delete(user.getId());
        }

        @Test
        void shouldThrowException_whenInvalidTokenTaken() {
            // given
            var req = RefreshRequest
                    .builder()
                    .refreshToken(REFRESH_TOKEN)
                    .build();
            when(jwtService.isTokenValid(req.getRefreshToken()))
                    .thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.logout(req))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }
}
