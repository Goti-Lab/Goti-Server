package com.goti.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResaleListingCreateRequest(
	@NotNull(message = "티켓 ID는 필수입니다.")
	UUID ticketId,

	@NotNull(message = "판매가는 필수입니다.")
	@Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
	Integer listingPrice
) {
}