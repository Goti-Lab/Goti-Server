package com.goti.global.dto;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "페이징 정보")
public record Paging(
	@Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
	@Schema(defaultValue = "1")
	int page,
	@Range(min = 1, max = 30, message = "페이지 사이즈는 1 ~ 30 사이여야 합니다.")
	@Schema(defaultValue = "10")
	int size
) {
	public Paging {
		if (page == 0) page = 1;
		if (size == 0) size = 10;
	}

	public Pageable toPageable() {
		return PageRequest.of(page - 1, size);
	}
}
