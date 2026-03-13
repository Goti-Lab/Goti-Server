package com.goti.order.repository;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.constants.OrderItemStatus;
import com.goti.domain.entity.order.OrderItemEntity;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID>, OrderItemRepositoryCustom {
	boolean existsOrderedSeats(
		UUID gameId,
		Collection<UUID> seatIds,
		Collection<OrderItemStatus> statuses
	);
}
