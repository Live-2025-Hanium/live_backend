package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c ORDER BY c.id ASC")
    List<Category> findAllOrderById();
} 