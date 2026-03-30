package com.goti.ticketing.ticket.service.domain;

import java.util.UUID;

import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;

public interface TicketFreezeService {
	TicketFreezeEntity freezeTicket(
		TicketEntity ticket,
		TicketFreezeReason freezeReason
	);

	boolean isFrozen(UUID ticketId);

	TicketFreezeInfo getCurrentFreeze(UUID ticketId);
}
