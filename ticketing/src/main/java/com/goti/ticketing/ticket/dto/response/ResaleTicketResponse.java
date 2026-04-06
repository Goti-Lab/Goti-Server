package com.goti.ticketing.ticket.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.ticketing.domain.entity.ticket.TicketEntity;

public record ResaleTicketResponse(
	UUID ticketId,
	UUID ownerId,
	UUID gameId,
	UUID seatId,
	UUID sectionId,
	UUID gradeId,
	String seatInfo,
	Integer ticketPrice,
	LocalDateTime gameDate,
	UUID transactionId,
	Instant createdAt
) {
	public static ResaleTicketResponse from(
		TicketEntity ticket,
		UUID seatId,
		UUID sectionId,
		UUID gradeId
	) {
		return new ResaleTicketResponse(
			ticket.getId(),
			ticket.getUserId(),
			ticket.getGameId(),
			seatId,
			sectionId,
			gradeId,
			ticket.getSeatInfo(),
			ticket.getTicketPrice(),
			ticket.getGameDate(),
			ticket.getResaleTransactionId(),
			ticket.getCreatedAt()
		);
	}
}
