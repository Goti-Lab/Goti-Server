package com.goti.order.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.order.dto.response.OrderListResponse;

public interface OrderService {
	OrderEntity create(
		UUID memberId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	);

	List<OrderListResponse> getMyOrders(UUID memberId);
}
