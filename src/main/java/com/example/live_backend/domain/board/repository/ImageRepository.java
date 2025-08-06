package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    
    Optional<Image> findByS3Url(String s3Url);
} 