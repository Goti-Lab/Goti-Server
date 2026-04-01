package com.goti.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미정산 금액 응답")
public record UnsettledAmountResponse(
	@Schema(description = "미정산 금액", example = "24000")
	Long unsettledAmount
) {
}