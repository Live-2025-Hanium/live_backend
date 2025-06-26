package com.example.live_backend.domain.example.controller;

import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.domain.example.dto.response.UserResponseDto;
import com.example.live_backend.domain.example.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseHandler<UserResponseDto> getUser(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseHandler.response(user);
    }
}
