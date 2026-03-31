package com.goti.resale.dto.response;

import java.util.Collection;
import java.util.List;

import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResaleListingOrderCreateResponse(
	@Schema(description = "리셀 주문 정보 목록")
	List<ResaleListingOrderSummaryResponse> orders,

	@Schema(description = "등록된 리셀 목록")
	List<ResaleListingResponse> listings
) {
	public static ResaleListingOrderCreateResponse from(
		Collection<ResaleListingOrderEntity> orders,
		List<ResaleListingResponse> listings
	) {
		return new ResaleListingOrderCreateResponse(
			orders.stream()
				.map(ResaleListingOrderSummaryResponse::from)
				.toList(),
			listings
		);
	}
}
