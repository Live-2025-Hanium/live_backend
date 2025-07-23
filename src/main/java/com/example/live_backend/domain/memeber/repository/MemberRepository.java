package com.example.live_backend.domain.memeber.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.live_backend.domain.memeber.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByKakaoId(String kakaoId);
	boolean existsByKakaoId(String kakaoId);
}