package com.goti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResaleTicketResponse(
	UUID ticketId,
	UUID gameId,
	UUID ownerId,
	String seatInfo,
	Integer ticketPrice,
	LocalDateTime gameDate
) {
}