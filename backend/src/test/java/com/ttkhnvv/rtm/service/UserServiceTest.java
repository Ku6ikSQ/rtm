package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.mapper.UserMapper;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UserService userService;
    private User user;

    @BeforeEach
    void init() {
        userService = new UserService(userRepository, userMapper, null, null, null);
        user = User
                .builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("user@email.com")
                .role(UserRole.USER)
                .isActive(true)
                .build();
    }

    @Nested
    class FindById {
        @Test
        void shouldReturnUserDto_whenUserExists() {
            // given
            var expected = UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .imageUrl(user.getImageKey())
                    .build();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // when
            var res = userService.getById(user.getId());

            // then
            assertThat(res).isEqualTo(expected);
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
}