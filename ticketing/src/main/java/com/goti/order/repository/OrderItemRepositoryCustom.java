package com.goti.order.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.goti.constants.OrderItemStatus;
import com.goti.domain.entity.order.OrderItemEntity;

public interface OrderItemRepositoryCustom {
	List<OrderItemEntity> findOrderItemsByOrderId(UUID orderId);

	boolean existsOrderedSeats(
		UUID gameId,
		Collection<UUID> seatIds,
		Collection<OrderItemStatus> statuses
	);
}
