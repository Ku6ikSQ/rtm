package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.mapper.UserMapper;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        var user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void block(UUID id) {
        var user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unblock(UUID id) {
        var user = findUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
        tokenRepository.delete(id);
    }

    @Transactional
    public void updateUsername(UUID id, String username) {
        var user = findUserById(id);
        user.setUsername(username);
        userRepository.save(user);
    }

    @Transactional
    public void updateEmail(UUID id, String email) {
        var user = findUserById(id);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(UUID id, String password) {
        var user = findUserById(id);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(id);
    }

    @Transactional
    public void updateAvatar(UUID id, MultipartFile file) {
        var user = findUserById(id);
        // TODO: store image into minIO and then use imageKey
        user.setImageKey(file.getOriginalFilename());
        userRepository.save(user);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Failed to find user."));
    }
}
