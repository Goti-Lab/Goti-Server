package com.goti.global.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "페이징 정보")
public record Paging(
	@Schema(defaultValue = "1")
	Integer page,
	@Schema(defaultValue = "10")
	Integer size
) {
	public Paging {
		if (page == null || page <= 0) page = 1;
		if (size == null || size <= 0) size = 10;
		if (size > 30) size = 30;
	}

	public Pageable toPageable() {
		return PageRequest.of(page - 1, size);
	}
}
