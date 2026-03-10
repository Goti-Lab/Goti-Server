package com.goti.order.service.domain;

import java.util.UUID;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;

public interface OrderService {
	OrderEntity create(
		UUID userId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	);
}
