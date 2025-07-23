package com.example.live_backend.domain.memeber.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.live_backend.domain.memeber.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByKakaoId(String kakaoId);

	@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.profile.nickname = :nickname")
	boolean existsByProfileNickname(@Param("nickname") String nickname);
	
	@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.profile.nickname = :nickname AND m.id != :memberId")
	boolean existsByProfileNicknameAndIdNot(@Param("nickname") String nickname, @Param("memberId") Long memberId);
}