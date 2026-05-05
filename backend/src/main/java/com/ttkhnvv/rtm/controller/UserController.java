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
}
