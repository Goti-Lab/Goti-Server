package com.goti.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record ResaleOrderRequest(
	@Schema(description = "리셀 점유 ID 목록", example = "[\"8df84c70-833e-4374-85ad-fa52f92f939e\"]")
	@NotEmpty(message = "리셀 점유 ID 목록은 필수입니다.")
	List<UUID> holdIds
) {
}
