package com.goti.resale.dto.response;

import com.goti.resale.constants.ResaleStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 현재 상태 응답")
public record ResaleStatusResponse(
	@Schema(description = "리셀 상태")
	ResaleStatus status
) {
}
