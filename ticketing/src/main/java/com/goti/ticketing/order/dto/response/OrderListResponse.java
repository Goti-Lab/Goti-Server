package com.goti.ticketing.order.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.goti.constants.OrderStatus;
import com.goti.ticketing.domain.entity.order.OrderEntity;

public record OrderListResponse(
	//TODO: 주문 조회 페이지 나오면 필드 조정
	UUID orderId,
	String orderNumber,
	OrderStatus orderStatus,
	Integer totalQuantity,
	Integer totalAmount,
	Instant orderedAt,
	UUID gameId,
	UUID stadiumId
) {
	public static OrderListResponse from(OrderEntity order) {
		return new OrderListResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderStatus(),
			order.getTotalQuantity(),
			order.getTotalAmount(),
			order.getCreatedAt(),
			order.getGameSchedule().getId(),
			order.getGameSchedule().getStadiumId()
		);
	}
}
