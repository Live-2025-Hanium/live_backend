package com.example.live_backend.domain.example.service;

import com.example.live_backend.domain.example.dto.request.ExampleRequestDto;
import com.example.live_backend.domain.example.dto.response.ExampleResponseDto;
import com.example.live_backend.domain.example.entity.Example;
import com.example.live_backend.domain.example.repository.ExampleRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    @Transactional(readOnly = true)
    public ExampleResponseDto getExampleById(Long id) {
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO, "예제를 찾을 수 없습니다."));
        return ExampleResponseDto.from(example);
    }

    @Transactional(readOnly = true)
    public List<ExampleResponseDto> getAllExamples() {
        List<Example> examples = exampleRepository.findAll();
        return examples.stream()
                .map(ExampleResponseDto::from)
                .toList();
    }

    @Transactional
    public ExampleResponseDto createExample(ExampleRequestDto request) {
        Example example = Example.of(request);
        exampleRepository.save(example);
        return ExampleResponseDto.from(example);
    }

    @Transactional
    public ExampleResponseDto updateExample(Long id, ExampleRequestDto request) {
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO, "예제를 찾을 수 없습니다."));
        example.update(request);
        return ExampleResponseDto.from(example);
    }

    @Transactional
    public void deleteExample(Long id) {
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO, "예제를 찾을 수 없습니다."));
        exampleRepository.delete(example);
    }
} 