package com.goti.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ResaleTransactionRequest(
	@Schema(description = "리셀 점유 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "리셀 점유 ID는 필수입니다.")
	UUID holdId
) {
}
