package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.mapper.UserMapper;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getById(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Failed to find user."));
        return userMapper.toResponse(user);
    }
}
