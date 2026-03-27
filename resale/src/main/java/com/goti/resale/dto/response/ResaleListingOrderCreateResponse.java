package com.goti.resale.dto.response;

import java.util.List;
import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResaleListingOrderCreateResponse(
	@Schema(description = "리셀 주문 ID")
	UUID orderId,

	@Schema(description = "리셀 주문 번호")
	String orderNumber,

	@Schema(description = "등록된 리셀 목록")
	List<ResaleListingResponse> listings
) {
	public static ResaleListingOrderCreateResponse from(
		ResaleListingOrderEntity order,
		List<ResaleListingResponse> listings
	) {
		return new ResaleListingOrderCreateResponse(
			order.getId(),
			order.getOrderNumber(),
			listings
		);
	}
}
