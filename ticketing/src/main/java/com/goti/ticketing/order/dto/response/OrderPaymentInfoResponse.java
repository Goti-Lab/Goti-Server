package com.goti.ticketing.order.dto.response;

import java.util.UUID;

import com.goti.constants.OrderStatus;
import com.goti.ticketing.domain.entity.order.OrderEntity;

public record OrderPaymentInfoResponse(
	UUID orderId,
	UUID memberId,
	OrderStatus orderStatus,
	Integer totalAmount
) {
	public static OrderPaymentInfoResponse from(OrderEntity order) {
		return new OrderPaymentInfoResponse(
			order.getId(),
			order.getMemberId(),
			order.getOrderStatus(),
			order.getTotalAmount()
		);
	}
}
