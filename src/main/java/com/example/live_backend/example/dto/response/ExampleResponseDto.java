package com.example.live_backend.example.dto.response;

import com.example.live_backend.example.entity.Example;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponseDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    
    public static ExampleResponseDto from(Example example) {
        return ExampleResponseDto.builder()
                .id(example.getId())
                .title(example.getTitle())
                .content(example.getContent())
                .author(example.getAuthor())
                .createdAt(example.getCreatedAt())
                .build();
    }
} 