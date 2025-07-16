package com.example.live_backend.domain.example.service;

import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.example.repository.UserJPARepository;
import com.example.live_backend.domain.example.dto.response.UserResponseDto;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Deprecated
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJPARepository userJPARepository;

    public UserResponseDto getUserById(Long id) {
        User user = userJPARepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserResponseDto(user);
    }
}
