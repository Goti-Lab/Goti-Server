package com.goti.resale.dto.response;

import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 주문 정보")
public record ResaleListingOrderSummaryResponse(
	@Schema(description = "리셀 주문 ID")
	UUID orderId,

	@Schema(description = "리셀 주문 번호")
	String orderNumber
) {
	public static ResaleListingOrderSummaryResponse from(ResaleListingOrderEntity order) {
		return new ResaleListingOrderSummaryResponse(
			order.getId(),
			order.getOrderNumber()
		);
	}
}
