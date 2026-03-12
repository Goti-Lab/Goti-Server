package com.goti.order.repository;

import java.util.Collection;
import java.util.UUID;

import com.goti.constants.OrderItemStatus;

public interface OrderItemRepositoryCustom {
	boolean existsOrderedSeats(
		UUID gameId,
		Collection<UUID> seatIds,
		Collection<OrderItemStatus> statuses
	);
}
