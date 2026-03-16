package com.goti.ticketing.order.service.domain;

import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderHistoryEntity;

public interface OrderHistoryService {
	OrderHistoryEntity create(
		OrderEntity order,
		String name,
		String phone,
		String email
	);
}
