package com.example.live_backend.domain.example.repository;

import com.example.live_backend.domain.example.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleRepository extends JpaRepository<Example, Long> {
} 