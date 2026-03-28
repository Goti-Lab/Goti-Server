package com.goti.ticketing.ticket.service.domain;

import java.time.LocalDateTime;

import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;

public record TicketFreezeInfo(
	TicketFreezeReason freezeReason,
	LocalDateTime frozenUntil
) {
	public static TicketFreezeInfo from(TicketFreezeEntity freeze) {
		return new TicketFreezeInfo(
			freeze.getFreezeReason(),
			freeze.getFrozenUntil()
		);
	}
}
