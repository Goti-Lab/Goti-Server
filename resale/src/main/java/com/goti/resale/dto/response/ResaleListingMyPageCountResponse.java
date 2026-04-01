package com.goti.resale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResaleListingMyPageCountResponse(
	@Schema(description = "판매 중인 개수", example = "5")
	Long listingCount,

	@Schema(description = "판매 완료 개수", example = "5")
	Long soldCount
) {
}