package com.goti.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResaleListingCreateRequest(
	@Schema(description = "티켓 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "티켓 ID는 필수입니다.")
	UUID ticketId,

	@Schema(description = "리셀 판매가", example = "50000")
	@NotNull(message = "판매가는 필수입니다.")
	@Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
	Integer listingPrice
) {
}