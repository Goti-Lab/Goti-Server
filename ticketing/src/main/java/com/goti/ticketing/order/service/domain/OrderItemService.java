package com.goti.ticketing.order.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;

public interface OrderItemService {
	OrderItemEntity create(
		OrderEntity order,
		SeatEntity seat,
		UUID holdId,
		TicketType ticketType,
		Integer ticketPrice
	);

	List<OrderItemEntity> get(UUID orderId);

	void expire(OrderItemEntity orderItem);
}
