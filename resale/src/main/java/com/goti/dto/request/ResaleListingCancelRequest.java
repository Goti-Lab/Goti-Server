package com.goti.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ResaleListingCancelRequest(
	@NotNull(message = "리스팅 ID는 필수입니다")
	UUID listingId
) {
}