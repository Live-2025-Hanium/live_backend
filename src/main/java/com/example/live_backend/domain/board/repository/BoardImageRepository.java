package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
} 