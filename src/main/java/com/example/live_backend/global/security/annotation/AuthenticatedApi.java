package com.example.live_backend.global.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 인증된 사용자만 접근 가능한 API를 명시하는 메타 어노테이션입니다.
 * 사용 예시:
 * {@code
 * @AuthenticatedApi(reason = "게시글 작성은 로그인한 사용자만 가능합니다")
 * @PostMapping("/api/posts")
 * public PostResponse createPost(@RequestBody PostRequest request) {
 *     // 게시글 생성 로직
 * }
 * }
 *
 * 주요 기능
 * - 로그인한 사용자만 API를 호출할 수 있습니다.
 * - JWT 토큰 검증이 자동으로 수행됩니다.
 * - 보안 정책을 코드에서 명확히 드러냅니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("isAuthenticated()")
public @interface AuthenticatedApi {

	String DEFAULT_REASON = "인증 필요";

	/**
	 * API의 인증 요구 사유를 명시 (문서화 목적)
	 */
	String reason() default DEFAULT_REASON;
}
