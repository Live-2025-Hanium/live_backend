package com.example.live_backend.global.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 인증이 필요 없는 공개 API를 명시하는 메타 어노테이션입니다.
 *
 * 사용 예시:
 * {@code
 * @PublicApi(reason = "게시글 조회는 누구나 가능합니다")
 * @GetMapping("/api/posts")
 * public List<Post> getAllPosts() {
 *     // 게시글 전체 조회 로직
 * }
 * }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("permitAll()")
public @interface PublicApi {

	String DEFAULT_REASON = "공개 API";

	/**
	 * API의 공개 사유를 명시 (문서화 목적)
	 */
	String reason() default DEFAULT_REASON;
}
