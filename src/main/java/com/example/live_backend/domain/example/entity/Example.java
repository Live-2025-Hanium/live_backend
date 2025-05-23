package com.example.live_backend.domain.example.entity;

import com.example.live_backend.domain.BaseEntity;
import com.example.live_backend.domain.example.dto.request.ExampleRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "examples")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Example extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public static Example of(ExampleRequestDto request) {
        return Example.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public void update(ExampleRequestDto request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.author = request.getAuthor();
    }
} 