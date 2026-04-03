package com.goti.global.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.Min;

public record Paging(
	@Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
	int page,
	@Min(value = 1, message = "페이지 사이즈는 1 이상이어야 합니다.")
	int size
) {
	public Pageable toPageable() {
		return PageRequest.of(page - 1, size);
	}
}
