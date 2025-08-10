package com.example.live_backend.global.page;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(description = "페이지 요청")
public record PageTemplate<T>(
	@Schema(description = "전체 데이터 수", example = "10000")
	long totalElements,
	@Schema(description = "총 페이지 수", example = "100")
	int totalPages,
	@Schema(description = "현재 페이지 수", example = "10")
	int currentPage,
	@Schema(description = "페이지 당 데이터 수", example = "10")
	int pageSize,
	@Schema(description = "다음 페이지 여부")
	boolean hasNext,
	@Schema(description = "데이터")
	@NonNull
	List<T> content
) {

	public static <T> PageTemplate<T> of(List<T> content, Pagination pagination) {
		return new PageTemplate<T>(
			pagination.getTotalElements(),
			pagination.getTotalPages(),
			pagination.getCurrentPage(),
			pagination.getPageSize(),
			pagination.hasNext(),
			Optional.ofNullable(content).orElse(List.of())
		);
	}
}
