package com.example.live_backend.global.page;

import java.util.Optional;

import lombok.Getter;

@Getter
public class Pagination {
	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 100;

	private final long totalElements;
	private final int currentPage;
	private final int pageSize;

	private Pagination(long totalElements, int currentPage, int pageSize) {
		this.totalElements = Math.max(0, totalElements);
		this.currentPage = Math.max(1, currentPage);
		this.pageSize = Math.min(pageSize, MAX_SIZE);
	}

	public static Pagination of(long totalElements, Integer currentPage, Integer pageSize) {
		int validPage = Optional.ofNullable(currentPage)
			.filter(p -> p > 0)
			.orElse(DEFAULT_PAGE);

		int validSize = Optional.ofNullable(pageSize)
			.filter(s -> s > 0)
			.orElse(DEFAULT_SIZE);

		return new Pagination(totalElements, validPage, validSize);
	}

	public static Pagination fromCondition(PageSearchCondition condition, long totalElements) {
		return of(totalElements, condition.page(), condition.size());
	}

	public int getTotalPages() {
		return (int)Math.ceil((double)totalElements / pageSize);
	}

	public long calculateOffset() {
		return (long)(currentPage - 1) * pageSize;
	}

	public boolean hasNext() {
		return currentPage < getTotalPages();
	}

	public boolean hasPrevious() {
		return currentPage > 1;
	}
}
