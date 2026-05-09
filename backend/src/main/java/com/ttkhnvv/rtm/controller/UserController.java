package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.user.*;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.security.constraint.HasRoleUser;
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

    @HasRoleTrusted
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        var user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @HasRoleTrusted
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable UUID id) {
        userService.block(id);
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable UUID id) {
        userService.unblock(id);
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getOwn() {
        return ResponseEntity.ok(userService.getById(getCurrentUserId()));
    }

    @HasRoleUser
    @PatchMapping("/me/username")
    public ResponseEntity<Void> updateOwnUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        userService.updateUsername(getCurrentUserId(), request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PatchMapping("/me/email")
    public ResponseEntity<Void> updateOwnEmail(@Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(getCurrentUserId(), request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateOwnPassword(@Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(getCurrentUserId(), request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateOwnAvatar(@RequestParam("file") MultipartFile file) {
        userService.updateAvatar(getCurrentUserId(), file);
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/username")
    public ResponseEntity<Void> updateUsername(@PathVariable UUID id, @Valid @RequestBody UpdateUsernameRequest request) {
        userService.updateUsername(id, request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/email")
    public ResponseEntity<Void> updateEmail(@PathVariable UUID id, @Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(id, request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID id, @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(id, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAvatar(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        userService.updateAvatar(id, file);
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteOwn() {
        userService.delete(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
