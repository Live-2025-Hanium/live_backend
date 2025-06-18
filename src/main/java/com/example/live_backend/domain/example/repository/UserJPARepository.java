package com.example.live_backend.domain.example.repository;

import com.example.live_backend.domain.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJPARepository extends JpaRepository<User, Long> {
    // Custom query methods can be defined here if needed

}
