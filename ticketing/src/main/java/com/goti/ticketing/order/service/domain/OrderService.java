package com.goti.ticketing.order.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.order.dto.response.OrderListResponse;

public interface OrderService {
	OrderEntity create(
		UUID memberId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	);

	List<OrderListResponse> getMyOrders(UUID memberId);
}
