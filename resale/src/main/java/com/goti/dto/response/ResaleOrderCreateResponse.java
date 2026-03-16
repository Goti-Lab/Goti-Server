package com.goti.dto.response;

import java.util.UUID;

import com.goti.constants.ResaleOrderStatus;
import com.goti.domain.entity.resale.ResaleOrderEntity;

public record ResaleOrderCreateResponse(
	UUID orderId,
	String orderNumber,
	ResaleOrderStatus orderStatus,
	int totalQuantity,
	int totalAmount
) {
	public static ResaleOrderCreateResponse from(
		ResaleOrderEntity order,
		int totalQuantity
	) {
		return new ResaleOrderCreateResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderStatus(),
			totalQuantity,
			order.getTotalAmount()
		);
	}
}
