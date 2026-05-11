package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.mapper.UserMapper;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("user@email.com")
                .passwordHash("hashed_password")
                .role(UserRole.USER)
                .isActive(true)
                .build();
        userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnUserResponse_whenUserHasNoAvatar() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // when
            var result = userService.getById(user.getId());

            // then
            assertThat(result).isEqualTo(userResponse);
            verify(storageService, never()).getPresignedUrl(any());
        }

        @Test
        void shouldReturnUserResponseWithPresignedUrl_whenUserHasAvatar() {
            // given
            user.setImageKey("avatar-key");
            userResponse.setImageUrl("http://presigned-url");
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);
            when(storageService.getPresignedUrl("avatar-key")).thenReturn("http://presigned-url");

            // when
            var result = userService.getById(user.getId());

            // then
            assertThat(result.getImageUrl()).isEqualTo("http://presigned-url");
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.getById(user.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Block {
        @Test
        void shouldDeactivateUser_whenUserExists() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            userService.block(user.getId());

            // then
            assertThat(user.getIsActive()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.block(user.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Unblock {
        @Test
        void shouldActivateUser_whenUserExists() {
            // given
            user.setIsActive(false);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            userService.unblock(user.getId());

            // then
            assertThat(user.getIsActive()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.unblock(user.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteUserAndRevokeToken() {
            // when
            userService.delete(user.getId());

            // then
            verify(userRepository).deleteById(user.getId());
            verify(tokenRepository).delete(user.getId());
        }
    }

    @Nested
    class UpdateUsername {
        @Test
        void shouldUpdateUsername_whenUserExists() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            userService.updateUsername(user.getId(), "new_username");

            // then
            assertThat(user.getUsername()).isEqualTo("new_username");
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.updateUsername(user.getId(), "new_username"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class UpdateEmail {
        @Test
        void shouldUpdateEmail_whenUserExists() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            userService.updateEmail(user.getId(), "new@email.com");

            // then
            assertThat(user.getEmail()).isEqualTo("new@email.com");
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.updateEmail(user.getId(), "new@email.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class UpdatePassword {
        @Test
        void shouldUpdatePasswordHashAndRevokeToken_whenUserExists() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("new_password")).thenReturn("new_hashed_password");

            // when
            userService.updatePassword(user.getId(), "new_password");

            // then
            assertThat(user.getPasswordHash()).isEqualTo("new_hashed_password");
            verify(userRepository).save(user);
            verify(tokenRepository).delete(user.getId());
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.updatePassword(user.getId(), "new_password"))
                    .isInstanceOf(UserNotFoundException.class);
            verify(tokenRepository, never()).delete(any());
        }
    }

    @Nested
    class UpdateAvatar {
        @Test
        void shouldUploadNewAvatar_whenUserHasNoPreviousAvatar() {
            // given
            var file = mock(MultipartFile.class);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(storageService.upload(file)).thenReturn("new-avatar-key");

            // when
            userService.updateAvatar(user.getId(), file);

            // then
            verify(storageService, never()).delete(anyString());
            assertThat(user.getImageKey()).isEqualTo("new-avatar-key");
            verify(userRepository).save(user);
        }

        @Test
        void shouldDeleteOldAvatarAndUploadNew_whenUserHasExistingAvatar() {
            // given
            user.setImageKey("old-avatar-key");
            var file = mock(MultipartFile.class);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(storageService.upload(file)).thenReturn("new-avatar-key");

            // when
            userService.updateAvatar(user.getId(), file);

            // then
            verify(storageService).delete("old-avatar-key");
            assertThat(user.getImageKey()).isEqualTo("new-avatar-key");
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            // given
            var file = mock(MultipartFile.class);
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.updateAvatar(user.getId(), file))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
