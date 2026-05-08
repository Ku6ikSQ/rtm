package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.user.*;
import com.ttkhnvv.rtm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;
import static com.ttkhnvv.rtm.security.util.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        var user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable UUID id) {
        userService.block(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable UUID id) {
        userService.unblock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserOwn() {
        return ResponseEntity.ok(userService.getById(getCurrentUserId()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        userService.delete(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/username")
    public ResponseEntity<Void> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        userService.updateUsername(getCurrentUserId(), request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/email")
    public ResponseEntity<Void> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(getCurrentUserId(), request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(getCurrentUserId(), request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAvatar(@RequestParam("file") MultipartFile file) {
        userService.updateAvatar(getCurrentUserId(), file);
        return ResponseEntity.noContent().build();
    }
}
