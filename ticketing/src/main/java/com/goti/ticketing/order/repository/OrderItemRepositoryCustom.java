package com.goti.ticketing.order.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.constants.OrderItemStatus;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;

public interface OrderItemRepositoryCustom {
	List<OrderItemEntity> findOrderItemsByOrderId(UUID orderId);

	boolean existsOrderedSeats(
		UUID gameId,
		Collection<UUID> seatIds,
		Collection<OrderItemStatus> statuses
	);
}
