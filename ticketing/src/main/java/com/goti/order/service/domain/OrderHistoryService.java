package com.goti.order.service.domain;

import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.order.OrderHistoryEntity;

public interface OrderHistoryService {
	OrderHistoryEntity create(
		OrderEntity order,
		String name,
		String phone,
		String email
	);
}
