package com.example.live_backend.domain.auth.token;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {}