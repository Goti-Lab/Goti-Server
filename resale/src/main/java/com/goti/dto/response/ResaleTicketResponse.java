package com.goti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResaleTicketResponse(
	UUID ticketId,
	UUID ownerId,
	UUID gameId,
	UUID seatId,
	UUID sectionId,
	UUID gradeId,
	String seatInfo,
	Integer ticketPrice,
	LocalDateTime gameDate
) {
}