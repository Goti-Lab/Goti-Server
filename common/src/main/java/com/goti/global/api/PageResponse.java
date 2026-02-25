package com.goti.global.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
	List<T> list,
	long totalCount,
	int totalPages
) {
	public PageResponse(Page<T> page) {
		this(
			page.getContent(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}
