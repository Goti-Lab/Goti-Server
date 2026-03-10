package com.goti.order.service.domain;

import com.goti.constants.TicketType;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.seat.SeatEntity;

public interface OrderItemService {
	OrderItemEntity create(
		OrderEntity order,
		SeatEntity seat,
		TicketType ticketType,
		Integer ticketPrice
	);
}
