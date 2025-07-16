package com.example.live_backend.domain.auth.token.repository;
import org.springframework.data.repository.CrudRepository;

import com.example.live_backend.domain.auth.token.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {}