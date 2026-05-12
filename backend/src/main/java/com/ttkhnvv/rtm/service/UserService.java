package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.entity.user.UserRole;
import com.ttkhnvv.rtm.exception.auth.InvalidPasswordException;
import com.ttkhnvv.rtm.exception.user.UserNotFoundException;
import com.ttkhnvv.rtm.mapper.UserMapper;
import com.ttkhnvv.rtm.repository.token.TokenRepository;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Manages user account operations including blocking, profile updates, password changes and avatar storage.
 * Password changes invalidate the active refresh token to force re-authentication.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final StorageService storageService;

    /**
     * Returns a paginated list of all users.
     * Avatar URLs in the response are presigned and valid for 60 minutes.
     *
     * @param pageable pagination and sorting parameters
     * @return page of user responses
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        var page = userRepository.findAll(pageable);
        var content = page.getContent().stream()
                .map(this::toResponseWithAvatarUrl)
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single user by their identifier.
     * The image URL in the response is presigned and valid for 60 minutes.
     *
     * @param id user identifier
     * @return user response with a presigned avatar URL if an avatar is present
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return toResponseWithAvatarUrl(findUserById(id));
    }

    /**
     * Deactivates a user account, preventing further logins.
     *
     * @param id user identifier
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void block(UUID id) {
        var user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivates a previously blocked user account.
     *
     * @param id user identifier
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void unblock(UUID id) {
        var user = findUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Permanently deletes a user account and removes their active refresh token from the token store.
     *
     * @param id user identifier
     */
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
        tokenRepository.delete(id);
    }

    /**
     * Updates the username of a user.
     *
     * @param id       user identifier
     * @param username new username
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void updateUsername(UUID id, String username) {
        var user = findUserById(id);
        user.setUsername(username);
        userRepository.save(user);
    }

    /**
     * Updates the email address of a user.
     *
     * @param id    user identifier
     * @param email new email address
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void updateEmail(UUID id, String email) {
        var user = findUserById(id);
        user.setEmail(email);
        userRepository.save(user);
    }

    /**
     * Changes the password of the currently authenticated user after verifying the current password.
     *
     * @param id              user identifier
     * @param currentPassword plain-text current password to verify
     * @param newPassword     new plain-text password (will be encoded before storage)
     * @throws UserNotFoundException    if no user was found with the given id
     * @throws InvalidPasswordException if currentPassword does not match the stored hash
     */
    @Transactional
    public void changeOwnPassword(UUID id, String currentPassword, String newPassword) {
        var user = findUserById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            throw new InvalidPasswordException("Current password is incorrect.");
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(id);
    }

    /**
     * Updates the password of any user (admin use). Does not verify the current password.
     *
     * @param id       user identifier
     * @param password new plain-text password (will be encoded before storage)
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void updatePassword(UUID id, String password) {
        var user = findUserById(id);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(id);
    }

    /**
     * Updates the role of a user.
     *
     * @param id   user identifier
     * @param role new role
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void updateRole(UUID id, UserRole role) {
        var user = findUserById(id);
        user.setRole(role);
        userRepository.save(user);
    }

    /**
     * Replaces the avatar of a user. Deletes the previous avatar from storage if one exists.
     *
     * @param id   user identifier
     * @param file new avatar image (jpeg, png or webp)
     * @throws UserNotFoundException if no user was found with the given id
     */
    @Transactional
    public void updateAvatar(UUID id, MultipartFile file) {
        var user = findUserById(id);
        if (user.getImageKey() != null)
            storageService.delete(user.getImageKey());
        var imageKey = storageService.upload(file);
        user.setImageKey(imageKey);
        userRepository.save(user);
    }

    private UserResponse toResponseWithAvatarUrl(User user) {
        var response = userMapper.toResponse(user);
        if (user.getImageKey() != null)
            response.setImageUrl(storageService.getPresignedUrl(user.getImageKey()));
        return response;
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Failed to find user."));
    }
}