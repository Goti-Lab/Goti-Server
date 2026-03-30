package com.goti.ticketing.order.service.domain;

import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderCancellationItemEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;

public interface OrderCancellationItemService {
	OrderCancellationItemEntity create(
		OrderCancellationEntity cancellation,
		OrderItemEntity item,
		Integer refundAmount,
		Integer feeAmount
	);
}
