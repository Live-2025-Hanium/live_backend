package com.example.live_backend.domain.clover.service;

import com.example.live_backend.domain.clover.repository.CloverRepository;
import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.domain.clover.entity.Clover;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CloverService {

    private final CloverRepository cloverRepository;

    public CloverResponseDto getCloverCount(Long memberId) {

        Clover clover = cloverRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new CloverResponseDto(clover.getCount());
    }
}
