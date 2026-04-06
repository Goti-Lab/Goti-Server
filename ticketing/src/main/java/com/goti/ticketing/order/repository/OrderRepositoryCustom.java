package com.goti.ticketing.order.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.order.OrderEntity;

public interface OrderRepositoryCustom {
	List<OrderEntity> findOrders(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	);
}
