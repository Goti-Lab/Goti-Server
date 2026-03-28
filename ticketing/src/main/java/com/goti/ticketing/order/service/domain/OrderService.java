package com.goti.ticketing.order.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.order.dto.response.OrderListResponse;
import com.goti.ticketing.order.dto.response.OrderPaymentInfoResponse;

public interface OrderService {
	OrderEntity create(
		UUID memberId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	);

	OrderEntity get(UUID orderId);

	void expire(OrderEntity order);

	List<OrderListResponse> getMyOrders(UUID memberId);

	OrderPaymentInfoResponse getPaymentOrder(UUID orderId, UUID memberId);

	OrderEntity get(UUID orderId, UUID memberId);

	void cancel(OrderEntity order);

	void partialCancel(OrderEntity order);
}
