package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        var user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable UUID id) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable UUID id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserOwn() {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/username")
    public ResponseEntity<Void> updateUsername() {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/me/email")
    public ResponseEntity<Void> updateEmail() {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword() {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<Void> updateAvatar() {
        return ResponseEntity.ok(null);
    }
}
