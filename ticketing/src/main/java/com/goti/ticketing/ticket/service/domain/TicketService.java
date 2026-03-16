package com.goti.ticketing.ticket.service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;

public interface TicketService {
	TicketEntity create(
		OrderItemEntity orderItem,
		UUID gameId,
		UUID userId,
		String userNickname,
		String userEmail,
		String userPhone,
		String gameTitle,
		LocalDateTime gameDate,
		String seatInfo,
		Integer ticketPrice
	);
}
