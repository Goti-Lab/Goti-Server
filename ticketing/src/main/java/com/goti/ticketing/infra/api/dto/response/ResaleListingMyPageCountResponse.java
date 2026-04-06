package com.goti.ticketing.infra.api.dto.response;

public record ResaleListingMyPageCountResponse(
	Long listingCount,
	Long soldCount
) {
}
