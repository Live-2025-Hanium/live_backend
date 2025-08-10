package com.example.live_backend.global.page;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(description = "커서 기반 무한 스크롤 응답")
public record CursorTemplate<R, T>(
	@Schema(description = "다음 페이지 여부", example = "true")
	boolean hasNext,

	@Schema(description = "다음 요청에 사용할 커서 (hasNext가 true일 때만, nullable)", example = "12345")
	R nextCursor,

	@Schema(description = "데이터")
	@NonNull
	List<T> content
) {

	public static <R, T> CursorTemplate<R, T> ofEmpty() {
		return new CursorTemplate<>(false, null, List.of());
	}

	public static <R, T> CursorTemplate<R, T> of(List<T> content) {
		return new CursorTemplate<>(
			false,
			null,
			Optional.ofNullable(content).orElse(List.of())
		);
	}

	public static <R, T> CursorTemplate<R, T> ofWithNextCursor(R nextCursor, List<T> content) {
		return new CursorTemplate<>(
			true,
			nextCursor,
			Optional.ofNullable(content).orElse(List.of())
		);
	}
}
